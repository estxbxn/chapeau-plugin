package com.whimsy.chapeau.listeners;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for player events that should be synchronized across servers.
 * Handles player join/quit events and updates the server manager accordingly.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class PlayerEventListener implements Listener {

    private final ChapeauPlugin plugin;

    /**
     * Creates a new PlayerEventListener.
     *
     * @param plugin The plugin instance
     */
    public PlayerEventListener(ChapeauPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player join events.
     * Updates the server's player count and broadcasts the change.
     *
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update player count after join
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int playerCount = Bukkit.getOnlinePlayers().size();
            plugin.getServerManager().updatePlayerCount(playerCount);

            // Send player sync message
            Map<String, Object> data = new HashMap<>();
            data.put("action", "join");
            data.put("playerName", event.getPlayer().getName());
            data.put("playerUuid", event.getPlayer().getUniqueId().toString());
            data.put("serverPlayerCount", playerCount);
            data.put("timestamp", System.currentTimeMillis());

            plugin.getMessagingService().sendBroadcast(MessageType.PLAYER_SYNC.getTypeName(), data);

            LoggerUtil.debug("Player joined: " + event.getPlayer().getName() + " (Total: " + playerCount + ")");
        }, 1L); // Delay by 1 tick to ensure the player is fully joined
    }

    /**
     * Handles player quit events.
     * Updates the server's player count and broadcasts the change.
     *
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Update player count after quit
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int playerCount = Bukkit.getOnlinePlayers().size();
            plugin.getServerManager().updatePlayerCount(playerCount);

            // Send player sync message
            Map<String, Object> data = new HashMap<>();
            data.put("action", "quit");
            data.put("playerName", event.getPlayer().getName());
            data.put("playerUuid", event.getPlayer().getUniqueId().toString());
            data.put("serverPlayerCount", playerCount);
            data.put("timestamp", System.currentTimeMillis());

            plugin.getMessagingService().sendBroadcast(MessageType.PLAYER_SYNC.getTypeName(), data);

            LoggerUtil.debug("Player quit: " + event.getPlayer().getName() + " (Total: " + playerCount + ")");
        }, 1L); // Delay by 1 tick to ensure the player is fully disconnected
    }
}
