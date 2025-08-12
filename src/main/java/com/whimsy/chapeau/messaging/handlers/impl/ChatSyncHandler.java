package com.whimsy.chapeau.messaging.handlers.impl;

import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.messaging.handlers.ChapeauMessageHandler;
import com.whimsy.chapeau.messaging.handlers.HandlerContext;
import com.whimsy.chapeau.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

/**
 * Handler for synchronizing chat messages between servers.
 * When a player sends a chat message, it's broadcasted to all other servers.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ChatSyncHandler implements ChapeauMessageHandler {

    @Override
    public String getMessageType() {
        return MessageType.CHAT_BROADCAST.getTypeName();
    }

    @Override
    public int getPriority() {
        return 10; // High priority for chat messages
    }

    @Override
    public boolean handleMessage(Message message, HandlerContext context) {
        try {
            // Extract chat data
            String playerName = message.getDataAsString("playerName");
            String chatMessage = message.getDataAsString("message");
            String sourceServer = message.getSourceServer();

            // Validate required data
            if (playerName == null || chatMessage == null || sourceServer == null) {
                LoggerUtil.warning("Received invalid chat sync message: missing required data");
                return false;
            }

            // Don't process messages from our own server
            if (sourceServer.equals(context.getCurrentServer().getServerId())) {
                return true; // Successfully ignored
            }

            // Create formatted message using config format
            Component formattedMessage = createFormattedChatMessage(playerName, chatMessage, sourceServer, context);

            // Broadcast to all online players on this server
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));

            // Also log to console
            LoggerUtil.debug("Chat from " + sourceServer + " - " + playerName + ": " + chatMessage);

            return true;

        } catch (Exception e) {
            LoggerUtil.warning("Error processing chat sync message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a formatted chat message component using the configured format.
     *
     * @param playerName   The player who sent the message
     * @param message      The chat message content
     * @param sourceServer The server the message came from
     * @param context      The handler context to access configuration
     * @return The formatted component
     */
    private Component createFormattedChatMessage(String playerName, String message, String sourceServer, HandlerContext context) {
        // Get format from configuration
        String format = context.getPlugin().getChapeauConfig().getChatSyncFormat();

        // Replace placeholders
        String formattedText = format
                .replace("{server}", sourceServer)
                .replace("{player}", playerName)
                .replace("{message}", message);

        // Return as component with appropriate colors
        return Component.text(formattedText).color(NamedTextColor.WHITE);
    }

    @Override
    public void onRegister() {
        LoggerUtil.info("Chat synchronization handler registered - cross-server chat enabled");
    }

    @Override
    public void onUnregister() {
        LoggerUtil.info("Chat synchronization handler unregistered - cross-server chat disabled");
    }
}
