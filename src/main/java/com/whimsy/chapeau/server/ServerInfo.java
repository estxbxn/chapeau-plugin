package com.whimsy.chapeau.server;

/**
 * Represents information about a server in the multi-server network.
 * Contains metadata about server status, capabilities, and connection information.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ServerInfo {

    private final String serverId;
    private final String serverName;
    private final ServerType serverType;
    private long lastHeartbeat;
    private ServerStatus status;
    private int playerCount;
    private String version;

    /**
     * Creates a new ServerInfo instance.
     *
     * @param serverId   The unique server identifier
     * @param serverName The server display name
     * @param serverType The type of server
     */
    public ServerInfo(String serverId, String serverName, ServerType serverType) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverType = serverType;
        this.lastHeartbeat = System.currentTimeMillis();
        this.status = ServerStatus.ONLINE;
        this.playerCount = 0;
        this.version = "Unknown";
    }

    /**
     * Gets the server ID.
     *
     * @return The unique server identifier
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets the server name.
     *
     * @return The server display name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Gets the server type.
     *
     * @return The server type
     */
    public ServerType getServerType() {
        return serverType;
    }

    /**
     * Gets the last heartbeat timestamp.
     *
     * @return The timestamp of the last heartbeat
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * Updates the last heartbeat timestamp.
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * Gets the server status.
     *
     * @return The current server status
     */
    public ServerStatus getStatus() {
        return status;
    }

    /**
     * Sets the server status.
     *
     * @param status The new server status
     */
    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    /**
     * Gets the player count.
     *
     * @return The number of players on this server
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Sets the player count.
     *
     * @param playerCount The number of players on this server
     */
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    /**
     * Gets the server version.
     *
     * @return The server version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the server version.
     *
     * @param version The server version string
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Checks if the server is considered online based on heartbeat timing and status.
     *
     * @param timeoutMs The timeout in milliseconds
     * @return True if the server is considered online
     */
    public boolean isOnline(long timeoutMs) {
        // If status is explicitly offline, return false regardless of heartbeat
        if (status == ServerStatus.OFFLINE || status == ServerStatus.SHUTTING_DOWN) {
            return false;
        }

        // Otherwise check heartbeat timing
        return (System.currentTimeMillis() - lastHeartbeat) < timeoutMs;
    }

    /**
     * Gets the time since the last heartbeat in milliseconds.
     *
     * @return The time since last heartbeat
     */
    public long getTimeSinceLastHeartbeat() {
        return System.currentTimeMillis() - lastHeartbeat;
    }

    @Override
    public String toString() {
        return String.format("ServerInfo{id='%s', name='%s', type=%s, status=%s, players=%d, lastHeartbeat=%d}",
                serverId, serverName, serverType, status, playerCount, lastHeartbeat);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ServerInfo that = (ServerInfo) obj;
        return serverId.equals(that.serverId);
    }

    @Override
    public int hashCode() {
        return serverId.hashCode();
    }
}
