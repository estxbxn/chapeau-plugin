package com.whimsy.chapeau.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message that can be sent between servers.
 * Contains metadata and payload data for inter-server communication.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class Message {

    private static final Gson GSON = new Gson();

    private final String id;
    private final String type;
    private final String sourceServer;
    private final String targetServer;
    private final long timestamp;
    private final Map<String, Object> data;

    /**
     * Creates a new Message instance.
     *
     * @param type         The message type
     * @param sourceServer The source server ID
     * @param targetServer The target server ID (null for broadcast)
     * @param data         The message data
     */
    public Message(String type, String sourceServer, String targetServer, Map<String, Object> data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.sourceServer = sourceServer;
        this.targetServer = targetServer;
        this.timestamp = System.currentTimeMillis();
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    /**
     * Creates a Message from a JSON string.
     *
     * @param json The JSON string
     * @return The Message instance, or null if parsing failed
     */
    public static Message fromJson(String json) {
        try {
            return GSON.fromJson(json, Message.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Converts this message to a JSON string.
     *
     * @return The JSON representation
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * Gets the message type.
     *
     * @return The message type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the source server ID.
     *
     * @return The source server ID
     */
    public String getSourceServer() {
        return sourceServer;
    }

    /**
     * Gets the target server ID.
     *
     * @return The target server ID, or null for broadcast messages
     */
    public String getTargetServer() {
        return targetServer;
    }


    /**
     * Gets a specific data value.
     *
     * @param key The data key
     * @return The data value, or null if not found
     */
    public Object getData(String key) {
        return data.get(key);
    }

    /**
     * Gets a specific data value as a string.
     *
     * @param key The data key
     * @return The data value as string, or null if not found or not a string
     */
    public String getDataAsString(String key) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : null;
    }

    /**
     * Gets a specific data value as an integer.
     *
     * @param key The data key
     * @return The data value as integer, or null if not found or not a number
     */
    public Integer getDataAsInt(String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Checks if this message is a broadcast message.
     *
     * @return True if this is a broadcast message
     */
    public boolean isBroadcast() {
        return targetServer == null;
    }

    /**
     * Checks if this message is targeted to a specific server.
     *
     * @param serverId The server ID to check
     * @return True if this message is targeted to the specified server
     */
    public boolean isTargetedTo(String serverId) {
        return serverId != null && serverId.equals(targetServer);
    }

    @Override
    public String toString() {
        return String.format("Message{id='%s', type='%s', source='%s', target='%s', timestamp=%d}",
                id, type, sourceServer, targetServer, timestamp);
    }
}
