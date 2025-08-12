package com.whimsy.chapeau.messaging.handlers.impl;

import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.messaging.handlers.ChapeauMessageHandler;
import com.whimsy.chapeau.messaging.handlers.HandlerContext;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Example custom handler showing how easy it is to create new message handlers.
 * This handler demonstrates various features like responding to messages,
 * accessing server information, and executing commands.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ExampleCustomHandler implements ChapeauMessageHandler {

    public static final String MESSAGE_TYPE = "example_custom";

    @Override
    public String getMessageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public int getPriority() {
        return 5; // Medium priority
    }

    @Override
    public boolean handleMessage(Message message, HandlerContext context) {
        try {
            // Extract data from the message
            String action = message.getDataAsString("action");
            String requester = message.getDataAsString("requester");

            LoggerUtil.info("Received custom message from " + message.getSourceServer() +
                    " - Action: " + action + ", Requester: " + requester);

            // Handle different actions
            switch (action != null ? action.toLowerCase() : "") {
                case "ping":
                    handlePingAction(message, context, requester);
                    break;

                case "player_count":
                    handlePlayerCountAction(message, context, requester);
                    break;

                case "execute_command":
                    handleExecuteCommandAction(message, context, requester);
                    break;

                case "server_info":
                    handleServerInfoAction(message, context, requester);
                    break;

                case "command_result":
                    handleCommandResultAction(message, context, requester);
                    break;

                default:
                    LoggerUtil.warning("Unknown action in custom message: " + action);
                    return false;
            }

            return true;

        } catch (Exception e) {
            LoggerUtil.warning("Error processing custom message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles ping action - responds with pong.
     */
    private void handlePingAction(Message message, HandlerContext context, String requester) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("action", "pong");
        responseData.put("originalRequester", requester);
        responseData.put("responseTime", System.currentTimeMillis());
        responseData.put("responder", context.getCurrentServer().getServerId());

        context.sendToServer(message.getSourceServer(), MESSAGE_TYPE, responseData);
        LoggerUtil.info("Sent pong response to " + message.getSourceServer());
    }

    /**
     * Handles player count request - sends current player count.
     */
    private void handlePlayerCountAction(Message message, HandlerContext context, String requester) {
        int playerCount = Bukkit.getOnlinePlayers().size();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("action", "player_count_response");
        responseData.put("originalRequester", requester);
        responseData.put("playerCount", playerCount);
        responseData.put("serverName", context.getCurrentServer().getServerName());
        responseData.put("timestamp", System.currentTimeMillis());

        context.sendToServer(message.getSourceServer(), MESSAGE_TYPE, responseData);
        LoggerUtil.info("Sent player count (" + playerCount + ") to " + message.getSourceServer());
    }

    /**
     * Handles command execution request.
     */
    private void handleExecuteCommandAction(Message message, HandlerContext context, String requester) {
        String command = message.getDataAsString("command");

        if (command == null || command.trim().isEmpty()) {
            LoggerUtil.warning("Execute command request with no command specified");
            return;
        }

        // Security: only allow specific commands for safety
        if (isCommandAllowed(command)) {
            // Execute command with callback for detailed result
            context.executeCommand(command, (success, error, output) -> {
                // Send detailed response back to requester
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("action", "command_result");
                responseData.put("originalRequester", requester);
                responseData.put("command", command);
                responseData.put("success", success);
                responseData.put("timestamp", System.currentTimeMillis());

                if (error != null) {
                    responseData.put("error", error);
                }
                if (output != null) {
                    responseData.put("output", output);
                }

                context.sendToServer(message.getSourceServer(), MESSAGE_TYPE, responseData);
                LoggerUtil.info("Executed command '" + command + "' with result: " + success +
                        (error != null ? " (Error: " + error + ")" : "") +
                        (output != null ? " (Output: " + output + ")" : ""));
            });
        } else {
            LoggerUtil.warning("Command not allowed: " + command);

            // Send rejection response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("action", "command_result");
            responseData.put("originalRequester", requester);
            responseData.put("command", command);
            responseData.put("success", false);
            responseData.put("error", "Command not allowed");
            responseData.put("timestamp", System.currentTimeMillis());

            context.sendToServer(message.getSourceServer(), MESSAGE_TYPE, responseData);
        }
    }

    /**
     * Handles server info request.
     */
    private void handleServerInfoAction(Message message, HandlerContext context, String requester) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("action", "server_info_response");
        responseData.put("originalRequester", requester);
        responseData.put("serverName", context.getCurrentServer().getServerName());
        responseData.put("serverType", context.getCurrentServer().getServerType().getTypeName());
        responseData.put("playerCount", Bukkit.getOnlinePlayers().size());
        responseData.put("maxPlayers", Bukkit.getMaxPlayers());
        responseData.put("bukkitVersion", Bukkit.getBukkitVersion());
        responseData.put("timestamp", System.currentTimeMillis());

        context.sendToServer(message.getSourceServer(), MESSAGE_TYPE, responseData);
        LoggerUtil.info("Sent server info to " + message.getSourceServer());
    }

    /**
     * Handles command result messages (responses from executed commands).
     */
    private void handleCommandResultAction(Message message, HandlerContext context, String requester) {
        String command = message.getDataAsString("command");
        Boolean success = (Boolean) message.getData("success");
        String error = message.getDataAsString("error");
        String output = message.getDataAsString("output");
        String originalRequester = message.getDataAsString("originalRequester");

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Command execution result from ").append(message.getSourceServer())
                .append(" - Command: '").append(command).append("'")
                .append(", Success: ").append(success);

        if (originalRequester != null) {
            logMessage.append(", Requested by: ").append(originalRequester);
        }

        if (error != null) {
            logMessage.append(", Error: ").append(error);
        }

        if (output != null) {
            logMessage.append(", Output: ").append(output);
        }

        LoggerUtil.info(logMessage.toString());
    }

    /**
     * Checks if a command is allowed to be executed remotely.
     * This is a security measure to prevent dangerous commands.
     */
    private boolean isCommandAllowed(String command) {
        String cmd = command.toLowerCase().trim();

        // Allow only safe commands
        return cmd.startsWith("say ") ||
                cmd.startsWith("tell ") ||
                cmd.equals("list") ||
                cmd.equals("tps") ||
                cmd.startsWith("chapeau ");
    }

    @Override
    public void onRegister() {
        LoggerUtil.info("Example custom handler registered - supports: ping, player_count, execute_command, server_info, command_result");
    }

    @Override
    public void onUnregister() {
        LoggerUtil.info("Example custom handler unregistered");
    }
}
