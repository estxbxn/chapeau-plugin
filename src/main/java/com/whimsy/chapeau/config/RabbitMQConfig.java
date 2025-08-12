package com.whimsy.chapeau.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * RabbitMQ-specific configuration settings.
 * Contains all settings related to RabbitMQ connection and messaging.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class RabbitMQConfig {

    // Connection settings
    private final String host;
    private final int port;
    private final String virtualHost;
    private final String username;
    private final String password;

    // Connection behavior settings
    private final int connectionTimeout;
    private final int networkRecoveryInterval;
    private final boolean automaticRecovery;

    // Exchange settings
    private final String exchangeName;
    private final String exchangeType;
    private final boolean exchangeDurable;

    // Queue settings
    private final String queuePrefix;
    private final boolean queueDurable;
    private final boolean queueAutoDelete;
    private final boolean queueExclusive;

    /**
     * Creates a new RabbitMQConfig instance.
     *
     * @param section The configuration section containing RabbitMQ settings
     */
    public RabbitMQConfig(ConfigurationSection section) {
        // Connection settings
        this.host = section.getString("host", "localhost");
        this.port = section.getInt("port", 5672);
        this.virtualHost = section.getString("virtualHost", "/");
        this.username = section.getString("username", "guest");
        this.password = section.getString("password", "guest");

        // Connection behavior settings
        ConfigurationSection connectionSection = section.getConfigurationSection("connection");
        if (connectionSection != null) {
            this.connectionTimeout = connectionSection.getInt("connectionTimeout", 30000);
            this.networkRecoveryInterval = connectionSection.getInt("networkRecoveryInterval", 5000);
            this.automaticRecovery = connectionSection.getBoolean("automaticRecovery", true);
        } else {
            this.connectionTimeout = 30000;
            this.networkRecoveryInterval = 5000;
            this.automaticRecovery = true;
        }

        // Exchange settings
        ConfigurationSection exchangeSection = section.getConfigurationSection("exchange");
        if (exchangeSection != null) {
            this.exchangeName = exchangeSection.getString("name", "chapeau.servers");
            this.exchangeType = exchangeSection.getString("type", "topic");
            this.exchangeDurable = exchangeSection.getBoolean("durable", true);
        } else {
            this.exchangeName = "chapeau.servers";
            this.exchangeType = "topic";
            this.exchangeDurable = true;
        }

        // Queue settings
        ConfigurationSection queueSection = section.getConfigurationSection("queue");
        if (queueSection != null) {
            this.queuePrefix = queueSection.getString("prefix", "chapeau.server.");
            this.queueDurable = queueSection.getBoolean("durable", true);
            this.queueAutoDelete = queueSection.getBoolean("autoDelete", false);
            this.queueExclusive = queueSection.getBoolean("exclusive", false);
        } else {
            this.queuePrefix = "chapeau.server.";
            this.queueDurable = true;
            this.queueAutoDelete = false;
            this.queueExclusive = false;
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getNetworkRecoveryInterval() {
        return networkRecoveryInterval;
    }

    public boolean isAutomaticRecovery() {
        return automaticRecovery;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public boolean isExchangeDurable() {
        return exchangeDurable;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public boolean isQueueDurable() {
        return queueDurable;
    }

    public boolean isQueueAutoDelete() {
        return queueAutoDelete;
    }

    public boolean isQueueExclusive() {
        return queueExclusive;
    }

    /**
     * Generates the queue name for a specific server.
     *
     * @param serverId The server identifier
     * @return The complete queue name
     */
    public String getQueueName(String serverId) {
        return queuePrefix + serverId;
    }
}
