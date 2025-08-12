package com.whimsy.chapeau.messaging.handlers;

import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.messaging.MessageType;

/**
 * Enhanced interface for handling messages with additional context and utilities.
 * Provides a more feature-rich way to handle messages compared to the basic MessageHandler.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public interface ChapeauMessageHandler {

    /**
     * Handles an incoming message with context.
     *
     * @param message The message to handle
     * @param context The handler context providing utilities and server information
     * @return True if the message was handled successfully, false otherwise
     */
    boolean handleMessage(Message message, HandlerContext context);

    /**
     * Gets the message type this handler processes.
     * For MultiTypeMessageHandler implementations, this method is not used.
     *
     * @return The message type string
     */
    default String getMessageType() {
        return MessageType.UNKNOWN.getTypeName();
    }

    /**
     * Gets the handler priority. Higher priority handlers are executed first.
     *
     * @return The priority (default: 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Checks if this handler should process the given message.
     * Override this for custom filtering logic.
     *
     * @param message The message to check
     * @return True if this handler should process the message
     */
    default boolean canHandle(Message message) {
        return getMessageType().equals(message.getType());
    }

    /**
     * Called when the handler is registered.
     * Use this for initialization if needed.
     */
    default void onRegister() {
        // Default: no initialization needed
    }

    /**
     * Called when the handler is unregistered.
     * Use this for cleanup if needed.
     */
    default void onUnregister() {
        // Default: no cleanup needed
    }
}
