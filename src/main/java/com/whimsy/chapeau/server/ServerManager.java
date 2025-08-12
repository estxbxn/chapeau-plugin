package com.whimsy.chapeau.server;

import com.whimsy.chapeau.config.ChapeauConfig;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.messaging.MessagingService;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages information about servers in the multi-server network.
 * Tracks server status, handles heartbeats, and provides server discovery.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ServerManager implements ServerManagerInternal {
    private static final long HEARTBEAT_TIMEOUT = 25000L; // 25 seconds (2.5x heartbeat interval)
    private static final long CLEANUP_INTERVAL = 300000L; // 5 minutes

    private final ChapeauConfig config;
    private final MessagingService messagingService;
    private final Map<String, ServerInfo> servers;
    private final ServerInfo currentServer;

    private BukkitTask cleanupTask;
    private boolean started;

    /**
     * Creates a new ServerManager.
     *
     * @param config           The plugin configuration
     * @param messagingService The messaging service
     */
    public ServerManager(ChapeauConfig config, MessagingService messagingService) {
        this.config = config;
        this.messagingService = messagingService;
        this.servers = new ConcurrentHashMap<>();
        this.started = false;

        // Create current server info
        ServerType serverType = ServerUtils.determineServerType(config.getServerId());
        this.currentServer = new ServerInfo(config.getServerId(), config.getServerName(), serverType);

        // Register this server
        servers.put(config.getServerId(), currentServer);
    }

    /**
     * Starts the server manager.
     */
    public void start() {
        if (started) {
            return;
        }

        // Start cleanup task
        startCleanupTask();

        started = true;
        LoggerUtil.info("Server manager started");
    }

    /**
     * Stops the server manager.
     */
    public void stop() {
        if (!started) {
            return;
        }

        // Cancel cleanup task
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }

        started = false;
        LoggerUtil.info("Server manager stopped");
    }

    /**
     * Gets information about a specific server.
     *
     * @param serverId The server ID
     * @return The server info, or null if not found
     */
    public ServerInfo getServerInfo(String serverId) {
        return servers.get(serverId);
    }

    /**
     * Gets information about the current server.
     *
     * @return The current server info
     */
    public ServerInfo getCurrentServerInfo() {
        return currentServer;
    }

    /**
     * Gets all known servers.
     *
     * @return A map of server ID to server info
     */
    public Map<String, ServerInfo> getAllServers() {
        return new HashMap<>(servers);
    }

    /**
     * Gets all online servers.
     *
     * @return A list of online servers
     */
    public List<ServerInfo> getOnlineServers() {
        List<ServerInfo> onlineServers = new ArrayList<>();
        for (ServerInfo server : servers.values()) {
            if (server.isOnline(HEARTBEAT_TIMEOUT)) {
                onlineServers.add(server);
            }
        }
        return onlineServers;
    }

    /**
     * Gets servers by type.
     *
     * @param serverType The server type to filter by
     * @return A list of servers of the specified type
     */
    public List<ServerInfo> getServersByType(ServerType serverType) {
        List<ServerInfo> result = new ArrayList<>();
        for (ServerInfo server : servers.values()) {
            if (server.getServerType() == serverType && server.isOnline(HEARTBEAT_TIMEOUT)) {
                result.add(server);
            }
        }
        return result;
    }

    /**
     * Gets the total player count across all servers.
     *
     * @return The total player count
     */
    public int getTotalPlayerCount() {
        return servers.values().stream()
                .filter(server -> server.isOnline(HEARTBEAT_TIMEOUT))
                .mapToInt(ServerInfo::getPlayerCount)
                .sum();
    }

    /**
     * Updates the current server's player count.
     *
     * @param playerCount The new player count
     */
    public void updatePlayerCount(int playerCount) {
        currentServer.setPlayerCount(playerCount);

        // Update our own heartbeat since we're still alive
        currentServer.updateHeartbeat();

        // Send status update
        Map<String, Object> data = new HashMap<>();
        data.put("playerCount", playerCount);
        data.put("status", currentServer.getStatus().getStatusName());
        data.put("serverType", currentServer.getServerType().getTypeName());
        data.put("version", currentServer.getVersion());

        messagingService.sendBroadcast(MessageType.STATUS_UPDATE.getTypeName(), data);
    }

    /**
     * Checks if the server manager is started.
     *
     * @return True if started, false otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Adds a server to the manager (used by ServerManagerHandler).
     *
     * @param server The server to add
     */
    @Override
    public void addServer(ServerInfo server) {
        servers.put(server.getServerId(), server);
    }

    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupOfflineServers();
            }
        }.runTaskTimerAsynchronously(
                Bukkit.getPluginManager().getPlugin("Chapeau"),
                20L * 10, // Start after 10 seconds
                20L * 15  // Repeat every 15 seconds
        );
    }

    private void cleanupOfflineServers() {
        long currentTime = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
            ServerInfo server = entry.getValue();

            // Don't remove current server
            if (server.getServerId().equals(config.getServerId())) {
                continue;
            }

            // Check if server is considered offline
            if (currentTime - server.getLastHeartbeat() > CLEANUP_INTERVAL) {
                server.setStatus(ServerStatus.OFFLINE);
                toRemove.add(entry.getKey());
            }
        }

        // Remove offline servers
        for (String serverId : toRemove) {
            servers.remove(serverId);
            LoggerUtil.info("Removed offline server: " + serverId);
        }
    }
}
