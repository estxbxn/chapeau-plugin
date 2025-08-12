package com.whimsy.chapeau.messaging.handlers;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.config.ChapeauConfig;
import com.whimsy.chapeau.messaging.MessagingService;
import com.whimsy.chapeau.server.ServerInfo;
import com.whimsy.chapeau.server.ServerManager;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Context object providing utilities and services to message handlers.
 * This gives handlers access to plugin services without tight coupling.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class HandlerContext {

    private final ChapeauPlugin plugin;
    private final MessagingService messagingService;
    private final ServerManager serverManager;
    private final ChapeauConfig config;

    /**
     * Creates a new HandlerContext.
     *
     * @param plugin The plugin instance
     */
    public HandlerContext(ChapeauPlugin plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getMessagingService();
        this.serverManager = plugin.getServerManager();
        this.config = plugin.getChapeauConfig();
    }

    /**
     * Gets the plugin instance.
     *
     * @return The Chapeau plugin instance
     */
    public ChapeauPlugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the messaging service for sending responses or additional messages.
     *
     * @return The messaging service
     */
    public MessagingService getMessagingService() {
        return messagingService;
    }

    /**
     * Gets the server manager for server information.
     *
     * @return The server manager
     */
    public ServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Gets the plugin configuration.
     *
     * @return The configuration instance
     */
    public ChapeauConfig getConfig() {
        return config;
    }


    /**
     * Gets the current server information.
     *
     * @return The current server info
     */
    public ServerInfo getCurrentServer() {
        return serverManager.getCurrentServerInfo();
    }

    /**
     * Gets information about a specific server.
     *
     * @param serverId The server ID
     * @return The server info, or null if not found
     */
    public ServerInfo getServerInfo(String serverId) {
        return serverManager.getServerInfo(serverId);
    }

    /**
     * Sends a broadcast message to all servers.
     *
     * @param messageType The message type
     * @param data        The message data
     * @return True if sent successfully
     */
    public boolean sendBroadcast(String messageType, Map<String, Object> data) {
        return messagingService.sendBroadcast(messageType, data);
    }

    /**
     * Sends a message to a specific server.
     *
     * @param targetServer The target server ID
     * @param messageType  The message type
     * @param data         The message data
     * @return True if sent successfully
     */
    public boolean sendToServer(String targetServer, String messageType, Map<String, Object> data) {
        return messagingService.sendToServer(targetServer, messageType, data);
    }

    /**
     * Gets a player by name on this server.
     *
     * @param playerName The player name
     * @return The player, or null if not found
     */
    public Player getPlayer(String playerName) {
        return Bukkit.getPlayer(playerName);
    }

    /**
     * Executes a command on this server.
     * Commands are executed synchronously on the main thread for safety.
     *
     * @param command The command to execute (without leading slash)
     * @return True if the command was executed
     */
    public boolean executeCommand(String command) {
        return executeCommand(command, null);
    }

    /**
     * Executes a command on this server with a callback for the result.
     * Commands are executed asynchronously on the main thread for safety.
     *
     * @param command  The command to execute (without leading slash)
     * @param callback Optional callback to handle the result (can be null)
     * @return True if the command was queued for execution
     */
    public boolean executeCommand(String command, CommandCallback callback) {
        if (command == null || command.trim().isEmpty()) {
            LoggerUtil.warning("Cannot execute empty command");
            if (callback != null) {
                callback.onComplete(false, "Empty command", null);
            }
            return false;
        }

        try {
            // Execute command on main thread to avoid async issues
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success;
                String error = null;
                String output = null;

                try {
                    // Capture command output if possible
                    long startTime = System.currentTimeMillis();
                    success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    long executionTime = System.currentTimeMillis() - startTime;

                    output = "Command executed in " + executionTime + "ms";

                    if (success) {
                        LoggerUtil.info("Successfully executed command: " + command);
                    } else {
                        LoggerUtil.warning("Command execution returned false: " + command);
                        error = "Command returned false";
                    }

                } catch (Exception e) {
                    success = false;
                    error = e.getMessage();
                    LoggerUtil.warning("Failed to execute command on main thread: " + command + " - " + error);
                }

                // Call callback if provided
                if (callback != null) {
                    callback.onComplete(success, error, output);
                }
            });

            return true; // Command was queued successfully

        } catch (Exception e) {
            LoggerUtil.warning("Failed to queue command for execution: " + command + " - " + e.getMessage());
            if (callback != null) {
                callback.onComplete(false, e.getMessage(), null);
            }
            return false;
        }
    }
}
