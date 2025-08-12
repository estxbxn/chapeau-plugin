package com.whimsy.chapeau.messaging.handlers;

/**
 * Interface for handlers that can process multiple message types.
 * Extends ChapeauMessageHandler to allow handling of multiple message types.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public interface MultiTypeMessageHandler extends ChapeauMessageHandler {

    /**
     * Gets all message types this handler can process.
     *
     * @return Array of message type strings
     */
    String[] getMessageTypes();
}
