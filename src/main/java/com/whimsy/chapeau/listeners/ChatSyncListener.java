package com.whimsy.chapeau.listeners;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.utils.LoggerUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for chat events to synchronize messages across servers.
 * Captures chat messages and broadcasts them to other servers via the messaging system.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ChatSyncListener implements Listener {

    private final ChapeauPlugin plugin;
    private final boolean enabled;

    /**
     * Creates a new ChatSyncListener.
     *
     * @param plugin The plugin instance
     */
    public ChatSyncListener(ChapeauPlugin plugin) {
        this.plugin = plugin;
        // Check if chat sync is enabled in config
        this.enabled = plugin.getChapeauConfig().isChatSyncEnabled();
    }

    /**
     * Handles async chat events and broadcasts them to other servers.
     *
     * @param event The async chat event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!enabled) {
            return;
        }

        if (!plugin.getMessagingService().isStarted()) {
            return;
        }

        try {
            // Extract chat information
            String playerName = event.getPlayer().getName();
            String playerUuid = event.getPlayer().getUniqueId().toString();

            // Convert message component to plain text
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            // Skip empty messages
            if (message.trim().isEmpty()) {
                return;
            }

            // Create message data
            Map<String, Object> data = new HashMap<>();
            data.put("playerName", playerName);
            data.put("playerUuid", playerUuid);
            data.put("message", message);
            data.put("timestamp", System.currentTimeMillis());
            data.put("serverName", plugin.getChapeauConfig().getServerName());

            // Broadcast to all other servers
            boolean success = plugin.getMessagingService().sendBroadcast(MessageType.CHAT_BROADCAST.getTypeName(), data);

            if (success) {
                LoggerUtil.debug("Broadcasted chat message from " + playerName + ": " + message);
            } else {
                LoggerUtil.warning("Failed to broadcast chat message from " + playerName);
            }

        } catch (Exception e) {
            LoggerUtil.warning("Error processing chat message for synchronization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if chat synchronization is enabled.
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
