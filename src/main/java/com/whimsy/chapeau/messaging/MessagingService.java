package com.whimsy.chapeau.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.whimsy.chapeau.config.ChapeauConfig;
import com.whimsy.chapeau.messaging.handlers.HandlerRegistry;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main messaging service for inter-server communication using RabbitMQ.
 * Handles message sending, receiving, and routing to appropriate handlers.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class MessagingService {

    private final ChapeauConfig config;
    private final RabbitMQConnectionManager connectionManager;
    private HandlerRegistry handlerRegistry;

    private String serverQueue;
    private BukkitTask heartbeatTask;
    private boolean started;

    /**
     * Creates a new MessagingService.
     *
     * @param config The plugin configuration
     */
    public MessagingService(ChapeauConfig config) {
        this.config = config;
        this.connectionManager = new RabbitMQConnectionManager(config.getRabbitMQConfig());
        this.started = false;
    }

    /**
     * Sets the handler registry for advanced message handling.
     * This should be called during plugin initialization.
     *
     * @param handlerRegistry The handler registry
     */
    public void setHandlerRegistry(HandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
        LoggerUtil.info("Handler registry configured for MessagingService");
    }

    /**
     * Starts the messaging service.
     *
     * @return True if started successfully, false otherwise
     */
    public boolean start() {
        if (started) {
            LoggerUtil.warning("Messaging service is already started");
            return true;
        }

        // Connect to RabbitMQ
        if (!connectionManager.connect()) {
            LoggerUtil.severe("Failed to connect to RabbitMQ - messaging service not started");
            return false;
        }

        // Declare server queue
        serverQueue = connectionManager.declareServerQueue(config.getServerId());
        if (serverQueue == null) {
            LoggerUtil.severe("Failed to declare server queue - messaging service not started");
            connectionManager.disconnect();
            return false;
        }

        // Start consuming messages
        if (!startMessageConsumer()) {
            LoggerUtil.severe("Failed to start message consumer - messaging service not started");
            connectionManager.disconnect();
            return false;
        }

        // Start heartbeat
        startHeartbeat();

        // Mark as started before sending startup message
        started = true;
        LoggerUtil.info("Messaging service started successfully");

        // Send startup message now that service is started
        sendStartupMessage();
        return true;
    }

    /**
     * Stops the messaging service.
     */
    public void stop() {
        if (!started) {
            return;
        }

        // Send shutdown message
        sendShutdownMessage();

        // Stop heartbeat
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }

        // Disconnect from RabbitMQ
        connectionManager.disconnect();

        started = false;
        LoggerUtil.info("Messaging service stopped");
    }

    /**
     * Sends a message to a specific server.
     *
     * @param message The message to send
     * @return True if sent successfully, false otherwise
     */
    public boolean sendMessage(Message message) {
        if (!started || !connectionManager.isConnected()) {
            LoggerUtil.warning("Cannot send message: messaging service not started or not connected");
            return false;
        }

        try {
            Channel channel = connectionManager.getChannel();
            String routingKey = message.isBroadcast() ? "broadcast" : message.getTargetServer();

            channel.basicPublish(
                    connectionManager.getConfig().getExchangeName(),
                    routingKey,
                    null,
                    message.toJson().getBytes(StandardCharsets.UTF_8)
            );

            LoggerUtil.debug("Sent message: " + message);

            return true;

        } catch (IOException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to send message: " + message, e);
            return false;
        }
    }

    /**
     * Sends a broadcast message to all servers.
     *
     * @param type The message type
     * @param data The message data
     * @return True if sent successfully, false otherwise
     */
    public boolean sendBroadcast(String type, Map<String, Object> data) {
        Message message = new Message(type, config.getServerId(), null, data);
        return sendMessage(message);
    }

    /**
     * Sends a message to a specific server.
     *
     * @param targetServer The target server ID
     * @param type         The message type
     * @param data         The message data
     * @return True if sent successfully, false otherwise
     */
    public boolean sendToServer(String targetServer, String type, Map<String, Object> data) {
        Message message = new Message(type, config.getServerId(), targetServer, data);
        return sendMessage(message);
    }

    /**
     * Checks if the messaging service is started.
     *
     * @return True if started, false otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Gets the connection manager.
     *
     * @return The connection manager instance
     */
    public RabbitMQConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Gets the handler registry for advanced message handling.
     *
     * @return The handler registry, or null if not set
     */
    public HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    private boolean startMessageConsumer() {
        try {
            Channel channel = connectionManager.getChannel();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String messageJson = new String(delivery.getBody(), StandardCharsets.UTF_8);

                // Parse message
                Message message = Message.fromJson(messageJson);
                if (message == null) {
                    LoggerUtil.warning("Received invalid message: " + messageJson);
                    return;
                }

                // Skip most messages from ourselves, except heartbeats for self-monitoring
                if (config.getServerId().equals(message.getSourceServer())) {
                    // Allow our own heartbeats through for self-monitoring
                    if (!MessageType.HEARTBEAT.getTypeName().equals(message.getType())) {
                        return;
                    }
                }

                // Check if message is for us
                if (!message.isBroadcast() && !message.isTargetedTo(config.getServerId())) {
                    return;
                }

                LoggerUtil.debug("Received message: " + message);

                // Handle message
                handleMessage(message);
            };

            // Start consuming from server queue (will receive both direct and broadcast messages)
            channel.basicConsume(serverQueue, true, deliverCallback, consumerTag -> {
            });

            return true;

        } catch (IOException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to start message consumer", e);
            return false;
        }
    }

    private void handleMessage(Message message) {
        if (handlerRegistry != null) {
            try {
                boolean handled = handlerRegistry.handleMessage(message);
                if (!handled) {
                    LoggerUtil.debug("No handler registered for message type: " + message.getType());
                }
            } catch (Exception e) {
                LoggerUtil.log(Level.WARNING, "Error in message handler for type: " + message.getType(), e);
            }
        } else {
            LoggerUtil.warning("Handler registry not available - message ignored: " + message.getType());
        }
    }

    private void startHeartbeat() {
        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", System.currentTimeMillis());
                data.put("serverName", config.getServerName());
                sendBroadcast(MessageType.HEARTBEAT.getTypeName(), data);
            }
        }.runTaskTimerAsynchronously(
                Bukkit.getPluginManager().getPlugin("Chapeau"),
                20L * 5,  // Start after 5 seconds
                20L * 10  // Repeat every 10 seconds
        );
    }

    private void sendStartupMessage() {
        Map<String, Object> data = new HashMap<>();
        data.put("serverName", config.getServerName());
        data.put("timestamp", System.currentTimeMillis());
        sendBroadcast(MessageType.SERVER_STARTUP.getTypeName(), data);
    }

    private void sendShutdownMessage() {
        Map<String, Object> data = new HashMap<>();
        data.put("serverName", config.getServerName());
        data.put("timestamp", System.currentTimeMillis());
        sendBroadcast(MessageType.SERVER_SHUTDOWN.getTypeName(), data);
    }
}
