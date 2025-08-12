package com.whimsy.chapeau.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Messaging-specific configuration settings.
 * Contains settings related to message handling, timeouts, and debugging.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class MessagingConfig {

    private final boolean debug;
    private final long timeout;
    private final int maxRetries;
    private final long retryDelay;

    /**
     * Creates a new MessagingConfig instance.
     *
     * @param section The configuration section containing messaging settings
     */
    public MessagingConfig(ConfigurationSection section) {
        if (section != null) {
            this.debug = section.getBoolean("debug", false);
            this.timeout = section.getLong("timeout", 10000L);
            this.maxRetries = section.getInt("maxRetries", 3);
            this.retryDelay = section.getLong("retryDelay", 1000L);
        } else {
            this.debug = false;
            this.timeout = 10000L;
            this.maxRetries = 3;
            this.retryDelay = 1000L;
        }
    }

    /**
     * Checks if debug logging is enabled for messages.
     *
     * @return True if debug logging is enabled
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Gets the message timeout in milliseconds.
     *
     * @return The timeout value
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Gets the maximum number of retry attempts for failed messages.
     *
     * @return The maximum retry attempts
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the retry delay in milliseconds.
     *
     * @return The retry delay
     */
    public long getRetryDelay() {
        return retryDelay;
    }
}
