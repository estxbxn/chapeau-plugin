package com.whimsy.chapeau.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration manager for the Chapeau plugin.
 * Handles loading and accessing configuration values from config.yml.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ChapeauConfig {

    private final FileConfiguration config;

    // Server configuration
    private final String serverId;
    private final String serverName;

    // RabbitMQ configuration
    private final RabbitMQConfig rabbitMQConfig;

    // Messaging configuration
    private final MessagingConfig messagingConfig;

    // Commands configuration
    private final boolean enableTestCommands;
    private final String commandPrefix;

    // Features configuration
    private final boolean chatSyncEnabled;
    private final String chatSyncFormat;

    /**
     * Creates a new ChapeauConfig instance.
     *
     * @param config The FileConfiguration to load values from
     */
    public ChapeauConfig(FileConfiguration config) {
        this.config = config;

        // Load server configuration
        this.serverId = config.getString("server.id", "server01");
        this.serverName = config.getString("server.name", "Server 01");

        // Load RabbitMQ configuration
        ConfigurationSection rabbitSection = config.getConfigurationSection("rabbitmq");
        this.rabbitMQConfig = new RabbitMQConfig(rabbitSection);

        // Load messaging configuration
        ConfigurationSection messagingSection = config.getConfigurationSection("messaging");
        this.messagingConfig = new MessagingConfig(messagingSection);

        // Load commands configuration
        this.enableTestCommands = config.getBoolean("commands.enableTestCommands", true);
        this.commandPrefix = config.getString("commands.prefix", "chapeau");

        // Load features configuration
        this.chatSyncEnabled = config.getBoolean("features.chatSync.enabled", true);
        this.chatSyncFormat = config.getString("features.chatSync.format", "[{server}] <{player}> {message}");
    }

    /**
     * Gets the unique server identifier.
     *
     * @return The server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets the server display name.
     *
     * @return The server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Gets the RabbitMQ configuration.
     *
     * @return The RabbitMQ configuration
     */
    public RabbitMQConfig getRabbitMQConfig() {
        return rabbitMQConfig;
    }

    /**
     * Gets the messaging configuration.
     *
     * @return The messaging configuration
     */
    public MessagingConfig getMessagingConfig() {
        return messagingConfig;
    }

    /**
     * Checks if test commands are enabled.
     *
     * @return True if test commands are enabled
     */
    public boolean isTestCommandsEnabled() {
        return enableTestCommands;
    }

    /**
     * Gets the command prefix.
     *
     * @return The command prefix
     */
    public String getCommandPrefix() {
        return commandPrefix;
    }

    /**
     * Checks if chat synchronization is enabled.
     *
     * @return True if chat sync is enabled
     */
    public boolean isChatSyncEnabled() {
        return chatSyncEnabled;
    }

    /**
     * Gets the chat sync format string.
     *
     * @return The chat format with placeholders
     */
    public String getChatSyncFormat() {
        return chatSyncFormat;
    }

    /**
     * Gets the underlying FileConfiguration.
     *
     * @return The FileConfiguration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
