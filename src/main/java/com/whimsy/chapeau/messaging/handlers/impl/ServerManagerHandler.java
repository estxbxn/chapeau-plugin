package com.whimsy.chapeau.messaging.handlers.impl;

import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.messaging.handlers.HandlerContext;
import com.whimsy.chapeau.messaging.handlers.MultiTypeMessageHandler;
import com.whimsy.chapeau.server.*;
import com.whimsy.chapeau.utils.LoggerUtil;

/**
 * Handler for server management messages (heartbeat, status updates, lifecycle).
 * This handler processes messages that affect server discovery and status tracking.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ServerManagerHandler implements MultiTypeMessageHandler {

    @Override
    public String[] getMessageTypes() {
        return new String[]{
                MessageType.HEARTBEAT.getTypeName(),
                MessageType.STATUS_UPDATE.getTypeName(),
                MessageType.SERVER_STARTUP.getTypeName(),
                MessageType.SERVER_SHUTDOWN.getTypeName()
        };
    }

    @Override
    public int getPriority() {
        return 90; // Very high priority for server management
    }

    @Override
    public boolean canHandle(Message message) {
        // Handle heartbeat, status update, startup, and shutdown messages
        String type = message.getType();
        return MessageType.HEARTBEAT.getTypeName().equals(type) ||
                MessageType.STATUS_UPDATE.getTypeName().equals(type) ||
                MessageType.SERVER_STARTUP.getTypeName().equals(type) ||
                MessageType.SERVER_SHUTDOWN.getTypeName().equals(type);
    }

    @Override
    public boolean handleMessage(Message message, HandlerContext context) {
        try {
            ServerManager serverManager = context.getServerManager();
            String messageType = message.getType();

            if (MessageType.HEARTBEAT.getTypeName().equals(messageType)) {
                return handleHeartbeat(message, serverManager, context);
            } else if (MessageType.STATUS_UPDATE.getTypeName().equals(messageType)) {
                return handleStatusUpdate(message, serverManager, context);
            } else if (MessageType.SERVER_STARTUP.getTypeName().equals(messageType)) {
                return handleServerStartup(message, serverManager, context);
            } else if (MessageType.SERVER_SHUTDOWN.getTypeName().equals(messageType)) {
                return handleServerShutdown(message, serverManager, context);
            }

            return false;

        } catch (Exception e) {
            LoggerUtil.warning("Error processing server management message: " + e.getMessage());
            return false;
        }
    }

    private boolean handleHeartbeat(Message message, ServerManager serverManager, HandlerContext context) {
        String serverId = message.getSourceServer();
        ServerInfo server = serverManager.getServerInfo(serverId);

        if (server == null) {
            // Create new server info
            String serverName = message.getDataAsString("serverName");
            if (serverName == null) {
                serverName = serverId;
            }

            ServerType serverType = ServerUtils.determineServerType(serverId);
            server = new ServerInfo(serverId, serverName, serverType);
            serverManager.addServer(server);

            LoggerUtil.info("Discovered new server: " + serverId + " (" + serverName + ")");
        }

        // Update heartbeat
        server.updateHeartbeat();
        server.setStatus(ServerStatus.ONLINE);

        return true;
    }

    private boolean handleStatusUpdate(Message message, ServerManager serverManager, HandlerContext context) {
        String serverId = message.getSourceServer();
        ServerInfo server = serverManager.getServerInfo(serverId);

        if (server != null) {
            // Update server info from message data
            Integer playerCount = message.getDataAsInt("playerCount");
            if (playerCount != null) {
                server.setPlayerCount(playerCount);
            }

            String statusName = message.getDataAsString("status");
            if (statusName != null) {
                server.setStatus(ServerStatus.fromString(statusName));
            }

            String version = message.getDataAsString("version");
            if (version != null) {
                server.setVersion(version);
            }

            server.updateHeartbeat();
        }

        return true;
    }

    private boolean handleServerStartup(Message message, ServerManager serverManager, HandlerContext context) {
        String serverId = message.getSourceServer();
        String serverName = message.getDataAsString("serverName");

        if (serverName == null) {
            serverName = serverId;
        }

        ServerType serverType = ServerUtils.determineServerType(serverId);
        ServerInfo server = new ServerInfo(serverId, serverName, serverType);
        server.setStatus(ServerStatus.ONLINE);

        // Add to server manager
        if (serverManager != null) {
            serverManager.addServer(server);
        }

        LoggerUtil.info("Server started: " + serverId + " (" + serverName + ")");

        return true;
    }

    private boolean handleServerShutdown(Message message, ServerManager serverManager, HandlerContext context) {
        String serverId = message.getSourceServer();
        ServerInfo server = serverManager.getServerInfo(serverId);

        if (server != null) {
            server.setStatus(ServerStatus.OFFLINE);
            // Update the heartbeat to mark when we received the shutdown notice
            server.updateHeartbeat();
            LoggerUtil.info("Server shutting down: " + serverId);
        }

        return true;
    }

    @Override
    public void onRegister() {
        LoggerUtil.info("Server manager handler registered - monitoring server discovery and status");
    }

    @Override
    public void onUnregister() {
        LoggerUtil.info("Server manager handler unregistered");
    }
}
