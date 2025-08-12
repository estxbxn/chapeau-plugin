package com.whimsy.chapeau.listeners;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for server-related events that should be synchronized across servers.
 * Handles server lifecycle events and system status changes.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ServerEventListener implements Listener {

    private final ChapeauPlugin plugin;

    /**
     * Creates a new ServerEventListener.
     *
     * @param plugin The plugin instance
     */
    public ServerEventListener(ChapeauPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles server load events.
     * Notifies other servers when this server has finished loading.
     *
     * @param event The server load event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        // Wait for messaging service to be fully initialized
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getMessagingService().isStarted()) {
                Map<String, Object> data = new HashMap<>();
                data.put("loadType", event.getType().name());
                data.put("serverVersion", Bukkit.getVersion());
                data.put("bukkitVersion", Bukkit.getBukkitVersion());
                data.put("timestamp", System.currentTimeMillis());

                plugin.getMessagingService().sendBroadcast(MessageType.STATUS_UPDATE.getTypeName(), data);

                LoggerUtil.info("Server load event broadcasted: " + event.getType().name());
            }
        }, 60L); // Wait 3 seconds for everything to initialize
    }
}
