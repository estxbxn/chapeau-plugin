package com.whimsy.chapeau.server;

/**
 * Enumeration of server types in the multi-server network.
 * Defines the different roles a server can have in the network.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public enum ServerType {

    /**
     * Game server - handles player gameplay
     */
    GAME_SERVER("game"),

    /**
     * Proxy server - handles player connections and routing
     */
    PROXY("proxy"),

    /**
     * Hub server - central server for player management
     */
    HUB("hub"),

    /**
     * Test server - used for testing purposes
     */
    TEST("test"),

    /**
     * Unknown server type
     */
    UNKNOWN("unknown");

    private final String typeName;

    /**
     * Creates a new ServerType with the specified type name.
     *
     * @param typeName The string representation of the server type
     */
    ServerType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the string representation of this server type.
     *
     * @return The server type name
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Gets a ServerType from its string representation.
     *
     * @param typeName The type name to lookup
     * @return The corresponding ServerType, or UNKNOWN if not found
     */
    public static ServerType fromString(String typeName) {
        if (typeName == null) {
            return UNKNOWN;
        }

        for (ServerType type : values()) {
            if (type.typeName.equalsIgnoreCase(typeName)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    /**
     * Checks if this server type can handle players.
     *
     * @return True if this server type can have players connected
     */
    public boolean canHandlePlayers() {
        return this == GAME_SERVER || this == HUB || this == TEST;
    }

    /**
     * Checks if this server type is a proxy.
     *
     * @return True if this is a proxy server
     */
    public boolean isProxy() {
        return this == PROXY;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
