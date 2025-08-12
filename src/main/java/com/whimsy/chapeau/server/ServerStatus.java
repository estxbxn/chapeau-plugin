package com.whimsy.chapeau.server;

/**
 * Enumeration of possible server statuses in the multi-server network.
 * Represents the current operational state of a server.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public enum ServerStatus {

    /**
     * Server is online and operational
     */
    ONLINE("online"),

    /**
     * Server is offline or unreachable
     */
    OFFLINE("offline"),

    /**
     * Server is starting up
     */
    STARTING("starting"),

    /**
     * Server is shutting down
     */
    SHUTTING_DOWN("shutting_down"),

    /**
     * Server is in maintenance mode
     */
    MAINTENANCE("maintenance"),

    /**
     * Server status is unknown
     */
    UNKNOWN("unknown");

    private final String statusName;

    /**
     * Creates a new ServerStatus with the specified status name.
     *
     * @param statusName The string representation of the server status
     */
    ServerStatus(String statusName) {
        this.statusName = statusName;
    }

    /**
     * Gets the string representation of this server status.
     *
     * @return The server status name
     */
    public String getStatusName() {
        return statusName;
    }

    /**
     * Gets a ServerStatus from its string representation.
     *
     * @param statusName The status name to lookup
     * @return The corresponding ServerStatus, or UNKNOWN if not found
     */
    public static ServerStatus fromString(String statusName) {
        if (statusName == null) {
            return UNKNOWN;
        }

        for (ServerStatus status : values()) {
            if (status.statusName.equalsIgnoreCase(statusName)) {
                return status;
            }
        }

        return UNKNOWN;
    }

    /**
     * Checks if the server is available for players.
     *
     * @return True if the server can accept players
     */
    public boolean isAvailable() {
        return this == ONLINE;
    }

    /**
     * Checks if the server is in a transitional state.
     *
     * @return True if the server is starting or shutting down
     */
    public boolean isTransitional() {
        return this == STARTING || this == SHUTTING_DOWN;
    }

    @Override
    public String toString() {
        return statusName;
    }
}
