package com.whimsy.chapeau.messaging;

/**
 * Enumeration of predefined message types for inter-server communication.
 * Provides a standardized set of message types for common operations.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public enum MessageType {

    /**
     * Server heartbeat message - indicates server is alive
     */
    HEARTBEAT("heartbeat"),

    /**
     * Server status update message
     */
    STATUS_UPDATE("status_update"),

    /**
     * Player data synchronization message
     */
    PLAYER_SYNC("player_sync"),

    /**
     * Chat message broadcast
     */
    CHAT_BROADCAST("chat_broadcast"),

    /**
     * Command execution request
     */
    COMMAND_REQUEST("command_request"),

    /**
     * Command execution response
     */
    COMMAND_RESPONSE("command_response"),

    /**
     * Server shutdown notification
     */
    SERVER_SHUTDOWN("server_shutdown"),

    /**
     * Server startup notification
     */
    SERVER_STARTUP("server_startup"),

    /**
     * Test message for debugging purposes
     */
    TEST_MESSAGE("test_message"),

    /**
     * Custom message type for plugin-specific use
     */
    CUSTOM("custom"),

    /**
     * Unknown message type - used when type is not recognized
     */
    UNKNOWN("unknown");


    private final String typeName;

    /**
     * Creates a new MessageType with the specified type name.
     *
     * @param typeName The string representation of the message type
     */
    MessageType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the string representation of this message type.
     *
     * @return The message type name
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Gets a MessageType from its string representation.
     *
     * @param typeName The type name to lookup
     * @return The corresponding MessageType, or CUSTOM if not found
     */
    public static MessageType fromString(String typeName) {
        if (typeName == null) {
            return CUSTOM;
        }

        for (MessageType type : values()) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }

        return CUSTOM;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
