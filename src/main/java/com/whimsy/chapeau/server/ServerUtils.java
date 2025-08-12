package com.whimsy.chapeau.server;

/**
 * Utility class for server-related operations.
 * Contains helper methods for server management and classification.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public final class ServerUtils {

    /**
     * Determines server type based on server ID.
     *
     * @param serverId The server ID
     * @return The determined server type
     */
    public static ServerType determineServerType(String serverId) {
        if (serverId == null) {
            return ServerType.UNKNOWN;
        }

        String lowerServerId = serverId.toLowerCase();

        if (lowerServerId.contains("proxy") || lowerServerId.contains("bungee") || lowerServerId.contains("velocity")) {
            return ServerType.PROXY;
        } else if (lowerServerId.contains("hub") || lowerServerId.contains("lobby")) {
            return ServerType.HUB;
        } else if (lowerServerId.contains("test")) {
            return ServerType.TEST;
        } else if (lowerServerId.startsWith("server") || lowerServerId.contains("game")) {
            return ServerType.GAME_SERVER;
        }

        return ServerType.GAME_SERVER; // Default to game server
    }
}
