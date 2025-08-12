package com.whimsy.chapeau.utils;

import com.whimsy.chapeau.ChapeauPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for centralized logging using the plugin's logger.
 * Provides convenient methods for different log levels and ensures consistency.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class LoggerUtil {

    private static Logger logger;

    /**
     * Initializes the logger utility with the plugin instance.
     *
     * @param plugin The plugin instance
     */
    public static void initialize(ChapeauPlugin plugin) {
        logger = plugin.getLogger();
    }

    /**
     * Gets the logger instance.
     *
     * @return The logger, or throws IllegalStateException if not initialized
     */
    private static Logger getLogger() {
        if (logger == null) {
            throw new IllegalStateException("LoggerUtil not initialized. Call initialize() first.");
        }
        return logger;
    }

    /**
     * Logs an info message.
     *
     * @param message The message to log
     */
    public static void info(String message) {
        getLogger().info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public static void warning(String message) {
        getLogger().warning(message);
    }

    /**
     * Logs a severe error message.
     *
     * @param message The message to log
     */
    public static void severe(String message) {
        getLogger().severe(message);
    }

    /**
     * Logs a message with a specific level.
     *
     * @param level   The log level
     * @param message The message to log
     */
    public static void log(Level level, String message) {
        getLogger().log(level, message);
    }

    /**
     * Logs a message with a specific level and throwable.
     *
     * @param level     The log level
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void log(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }

    /**
     * Logs a debug message (only if debug is enabled).
     *
     * @param message The debug message to log
     */
    public static void debug(String message) {
        // Check if debug is enabled in messaging config
        ChapeauPlugin plugin = ChapeauPlugin.getInstance();
        if (plugin != null && plugin.getChapeauConfig() != null &&
                plugin.getChapeauConfig().getMessagingConfig().isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
