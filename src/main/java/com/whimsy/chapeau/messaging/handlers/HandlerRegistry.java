package com.whimsy.chapeau.messaging.handlers;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.messaging.Message;
import com.whimsy.chapeau.utils.LoggerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing message handlers.
 * Provides a centralized way to register, unregister, and execute handlers.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class HandlerRegistry {

    private final Map<String, List<ChapeauMessageHandler>> handlers;
    private final HandlerContext context;

    /**
     * Creates a new HandlerRegistry.
     *
     * @param plugin The plugin instance
     */
    public HandlerRegistry(ChapeauPlugin plugin) {
        this.handlers = new ConcurrentHashMap<>();
        this.context = new HandlerContext(plugin);
    }

    /**
     * Registers a message handler.
     *
     * @param handler The handler to register
     */
    public void registerHandler(ChapeauMessageHandler handler) {
        // Check if this handler can handle multiple message types
        if (handler instanceof MultiTypeMessageHandler multiHandler) {
            String[] messageTypes = multiHandler.getMessageTypes();

            for (String messageType : messageTypes) {
                handlers.computeIfAbsent(messageType, k -> new ArrayList<>()).add(handler);

                // Sort by priority (highest first)
                handlers.get(messageType).sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
            }

            // Call initialization
            try {
                handler.onRegister();
                LoggerUtil.info("Registered multi-type message handler for types: " + String.join(", ", messageTypes) +
                        " (Priority: " + handler.getPriority() + ")");
            } catch (Exception e) {
                LoggerUtil.warning("Error during handler registration: " + e.getMessage());
            }
        } else {
            // Single type handler
            String messageType = handler.getMessageType();

            handlers.computeIfAbsent(messageType, k -> new ArrayList<>()).add(handler);

            // Sort by priority (highest first)
            handlers.get(messageType).sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));

            // Call initialization
            try {
                handler.onRegister();
                LoggerUtil.info("Registered message handler for type: " + messageType +
                        " (Priority: " + handler.getPriority() + ")");
            } catch (Exception e) {
                LoggerUtil.warning("Error during handler registration for " + messageType + ": " + e.getMessage());
            }
        }
    }

    /**
     * Unregisters a specific handler instance.
     *
     * @param handler The handler to unregister
     * @return True if the handler was found and removed
     */
    public boolean unregisterHandler(ChapeauMessageHandler handler) {
        boolean removed = false;

        if (handler instanceof MultiTypeMessageHandler multiHandler) {
            String[] messageTypes = multiHandler.getMessageTypes();

            for (String messageType : messageTypes) {
                List<ChapeauMessageHandler> typeHandlers = handlers.get(messageType);
                if (typeHandlers != null && typeHandlers.remove(handler)) {
                    removed = true;

                    // Remove empty lists
                    if (typeHandlers.isEmpty()) {
                        handlers.remove(messageType);
                    }
                }
            }

            if (removed) {
                // Call cleanup once
                try {
                    handler.onUnregister();
                    LoggerUtil.info("Unregistered multi-type message handler for types: " + String.join(", ", messageTypes));
                } catch (Exception e) {
                    LoggerUtil.warning("Error during handler unregistration: " + e.getMessage());
                }
            }
        } else {
            String messageType = handler.getMessageType();
            List<ChapeauMessageHandler> typeHandlers = handlers.get(messageType);

            if (typeHandlers != null && typeHandlers.remove(handler)) {
                removed = true;

                // Call cleanup
                try {
                    handler.onUnregister();
                    LoggerUtil.info("Unregistered message handler for type: " + messageType);
                } catch (Exception e) {
                    LoggerUtil.warning("Error during handler unregistration for " + messageType + ": " + e.getMessage());
                }

                // Remove empty lists
                if (typeHandlers.isEmpty()) {
                    handlers.remove(messageType);
                }
            }
        }

        return removed;
    }

    /**
     * Unregisters all handlers for a specific message type.
     *
     * @param messageType The message type
     * @return The number of handlers that were unregistered
     */
    public int unregisterAllHandlers(String messageType) {
        List<ChapeauMessageHandler> typeHandlers = handlers.remove(messageType);

        if (typeHandlers != null) {
            int count = typeHandlers.size();

            // Call cleanup on all handlers
            for (ChapeauMessageHandler handler : typeHandlers) {
                try {
                    handler.onUnregister();
                } catch (Exception e) {
                    LoggerUtil.warning("Error during handler cleanup for " + messageType + ": " + e.getMessage());
                }
            }

            LoggerUtil.info("Unregistered " + count + " handlers for type: " + messageType);
            return count;
        }

        return 0;
    }

    /**
     * Handles a message by passing it to all appropriate handlers.
     *
     * @param message The message to handle
     * @return True if at least one handler processed the message successfully
     */
    public boolean handleMessage(Message message) {
        List<ChapeauMessageHandler> messageHandlers = handlers.get(message.getType());

        if (messageHandlers == null || messageHandlers.isEmpty()) {
            return false;
        }

        boolean handled = false;

        for (ChapeauMessageHandler handler : messageHandlers) {
            try {
                if (handler.canHandle(message)) {
                    boolean result = handler.handleMessage(message, context);
                    if (result) {
                        handled = true;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warning("Error in message handler for type " + message.getType() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return handled;
    }

    /**
     * Gets all registered message types.
     *
     * @return A set of registered message types
     */
    public Set<String> getRegisteredTypes() {
        return new HashSet<>(handlers.keySet());
    }

    /**
     * Gets the number of handlers for a specific message type.
     *
     * @param messageType The message type
     * @return The number of handlers
     */
    public int getHandlerCount(String messageType) {
        List<ChapeauMessageHandler> typeHandlers = handlers.get(messageType);
        return typeHandlers != null ? typeHandlers.size() : 0;
    }

    /**
     * Gets the total number of registered handlers.
     *
     * @return The total handler count
     */
    public int getTotalHandlerCount() {
        return handlers.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Clears all registered handlers.
     */
    public void clear() {
        // Call cleanup on all handlers
        for (List<ChapeauMessageHandler> typeHandlers : handlers.values()) {
            for (ChapeauMessageHandler handler : typeHandlers) {
                try {
                    handler.onUnregister();
                } catch (Exception e) {
                    LoggerUtil.warning("Error during handler cleanup: " + e.getMessage());
                }
            }
        }

        int totalCount = getTotalHandlerCount();
        handlers.clear();
        LoggerUtil.info("Cleared all " + totalCount + " registered handlers");
    }

    /**
     * Gets information about all registered handlers.
     *
     * @return A map of message type to handler count
     */
    public Map<String, Integer> getHandlerInfo() {
        Map<String, Integer> info = new HashMap<>();
        for (Map.Entry<String, List<ChapeauMessageHandler>> entry : handlers.entrySet()) {
            info.put(entry.getKey(), entry.getValue().size());
        }
        return info;
    }
}
