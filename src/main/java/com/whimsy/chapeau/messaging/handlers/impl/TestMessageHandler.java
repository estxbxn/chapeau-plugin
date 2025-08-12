package com.whimsy.chapeau.messaging.handlers.impl;

import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.messaging.handlers.ChapeauMessageHandler;
import com.whimsy.chapeau.messaging.handlers.HandlerContext;
import com.whimsy.chapeau.utils.LoggerUtil;

/**
 * Handler for processing test messages between servers.
 * Provides feedback and logging for testing inter-server communication.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class TestMessageHandler implements ChapeauMessageHandler {

    @Override
    public String getMessageType() {
        return MessageType.TEST_MESSAGE.getTypeName();
    }

    @Override
    public int getPriority() {
        return 20; // High priority for test messages
    }

    @Override
    public boolean handleMessage(Message message, HandlerContext context) {
        try {
            String messageContent = message.getDataAsString("message");
            String sender = message.getDataAsString("sender");
            String sourceServer = message.getSourceServer();

            // Log the received test message
            LoggerUtil.info("Received test message from " + sourceServer +
                    " (sender: " + sender + "): " + messageContent);

            return true;

        } catch (Exception e) {
            LoggerUtil.warning("Error processing test message from " + message.getSourceServer() + ": " + e.getMessage());
            return false;
        }
    }

}
