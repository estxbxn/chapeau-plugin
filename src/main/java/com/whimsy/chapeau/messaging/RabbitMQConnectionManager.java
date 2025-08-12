package com.whimsy.chapeau.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.whimsy.chapeau.config.RabbitMQConfig;
import com.whimsy.chapeau.utils.LoggerUtil;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Manages RabbitMQ connections and provides methods for connecting/disconnecting.
 * Handles connection recovery and provides access to channels for messaging operations.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class RabbitMQConnectionManager {

    private final RabbitMQConfig config;
    private Connection connection;
    private Channel channel;
    private boolean connected;

    /**
     * Creates a new RabbitMQConnectionManager.
     *
     * @param config The RabbitMQ configuration
     */
    public RabbitMQConnectionManager(RabbitMQConfig config) {
        this.config = config;
        this.connected = false;
    }

    /**
     * Establishes a connection to RabbitMQ.
     *
     * @return True if connection was successful, false otherwise
     */
    public boolean connect() {
        if (connected) {
            LoggerUtil.warning("Already connected to RabbitMQ");
            return true;
        }

        try {
            // Create connection factory
            ConnectionFactory factory = getConnectionFactory();

            // Create connection and channel
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Set up exchange
            channel.exchangeDeclare(
                    config.getExchangeName(),
                    config.getExchangeType(),
                    config.isExchangeDurable()
            );

            connected = true;
            LoggerUtil.info("Successfully connected to RabbitMQ at " + config.getHost() + ":" + config.getPort());

            return true;

        } catch (IOException | TimeoutException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to connect to RabbitMQ", e);
            connected = false;
            return false;
        }
    }

    /**
     * Creates a RabbitMQ ConnectionFactory based on the configuration.
     *
     * @return The configured ConnectionFactory
     */
    private ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setVirtualHost(config.getVirtualHost());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());

        // Set connection settings
        factory.setConnectionTimeout(config.getConnectionTimeout());
        factory.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
        factory.setAutomaticRecoveryEnabled(config.isAutomaticRecovery());
        return factory;
    }

    /**
     * Disconnects from RabbitMQ.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }

            if (connection != null && connection.isOpen()) {
                connection.close();
            }

            connected = false;
            LoggerUtil.info("Disconnected from RabbitMQ");

        } catch (IOException | TimeoutException e) {
            LoggerUtil.log(Level.WARNING, "Error while disconnecting from RabbitMQ", e);
        } finally {
            channel = null;
            connection = null;
            connected = false;
        }
    }

    /**
     * Checks if currently connected to RabbitMQ.
     *
     * @return True if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && connection != null && connection.isOpen()
                && channel != null && channel.isOpen();
    }

    /**
     * Gets the RabbitMQ channel for messaging operations.
     *
     * @return The channel, or null if not connected
     */
    public Channel getChannel() {
        return isConnected() ? channel : null;
    }

    /**
     * Gets the RabbitMQ connection.
     *
     * @return The connection, or null if not connected
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gets the RabbitMQ configuration.
     *
     * @return The configuration instance
     */
    public RabbitMQConfig getConfig() {
        return config;
    }

    /**
     * Declares a queue for the specified server.
     *
     * @param serverId The server ID
     * @return The queue name, or null if failed
     */
    public String declareServerQueue(String serverId) {
        if (!isConnected()) {
            LoggerUtil.warning("Cannot declare queue: not connected to RabbitMQ");
            return null;
        }

        try {
            String queueName = config.getQueueName(serverId);
            channel.queueDeclare(
                    queueName,
                    config.isQueueDurable(),
                    config.isQueueExclusive(),
                    config.isQueueAutoDelete(),
                    null
            );

            // Bind queue to exchange for specific server messages
            channel.queueBind(queueName, config.getExchangeName(), serverId);

            // Also bind to broadcast messages (all servers should receive broadcasts)
            channel.queueBind(queueName, config.getExchangeName(), "broadcast");

            LoggerUtil.info("Declared queue: " + queueName + " (bound to " + serverId + " and broadcast)");
            return queueName;

        } catch (IOException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to declare queue for server: " + serverId, e);
            return null;
        }
    }
}
