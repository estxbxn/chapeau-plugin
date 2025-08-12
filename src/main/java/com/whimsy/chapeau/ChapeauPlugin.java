package com.whimsy.chapeau;

import com.whimsy.chapeau.commands.ChapeauCommand;
import com.whimsy.chapeau.config.ChapeauConfig;
import com.whimsy.chapeau.listeners.ChatSyncListener;
import com.whimsy.chapeau.listeners.PlayerEventListener;
import com.whimsy.chapeau.listeners.ServerEventListener;
import com.whimsy.chapeau.messaging.MessagingService;
import com.whimsy.chapeau.messaging.handlers.HandlerRegistry;
import com.whimsy.chapeau.messaging.handlers.impl.ChatSyncHandler;
import com.whimsy.chapeau.messaging.handlers.impl.ExampleCustomHandler;
import com.whimsy.chapeau.messaging.handlers.impl.ServerManagerHandler;
import com.whimsy.chapeau.messaging.handlers.impl.TestMessageHandler;
import com.whimsy.chapeau.server.ServerManager;
import com.whimsy.chapeau.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for the Chapeau multi-server testing plugin.
 * Initializes and manages all plugin components including messaging, server management, and commands.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ChapeauPlugin extends JavaPlugin {

    private static ChapeauPlugin instance;

    // Plugin components
    private ChapeauConfig config;
    private MessagingService messagingService;
    private ServerManager serverManager;
    private HandlerRegistry handlerRegistry;

    // Event listeners
    private PlayerEventListener playerEventListener;
    private ServerEventListener serverEventListener;
    private ChatSyncListener chatSyncListener;

    @Override
    public void onLoad() {
        instance = this;
        // Initialize logger utility
        LoggerUtil.initialize(this);
        LoggerUtil.info("Chapeau Plugin loading...");
    }

    @Override
    public void onEnable() {
        try {
            // Load configuration
            loadConfiguration();

            // Initialize components
            initializeComponents();

            // Register event listeners
            registerEventListeners();

            // Register commands
            registerCommands();

            // Start services
            startServices();

            LoggerUtil.info("Chapeau Plugin v" + this.getPluginMeta().getVersion() + " enabled successfully!");
            LoggerUtil.info("Server ID: " + config.getServerId() + " | Server Name: " + config.getServerName());

        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Failed to enable Chapeau Plugin", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Stop services
            stopServices();

            LoggerUtil.info("Chapeau Plugin disabled successfully");

        } catch (Exception e) {
            LoggerUtil.log(Level.WARNING, "Error during plugin shutdown", e);
        }
    }

    /**
     * Reloads the plugin configuration and restarts services.
     */
    public void reloadPlugin() {
        try {
            LoggerUtil.info("Reloading Chapeau Plugin...");

            // Stop services
            stopServices();

            // Reload configuration
            super.reloadConfig();
            loadConfiguration();

            // Reinitialize components
            initializeComponents();

            // Start services
            startServices();

            LoggerUtil.info("Chapeau Plugin reloaded successfully");

        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Failed to reload Chapeau Plugin", e);
        }
    }

    /**
     * Loads the plugin configuration.
     */
    private void loadConfiguration() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Load configuration
        config = new ChapeauConfig(super.getConfig());

        LoggerUtil.info("Configuration loaded for server: " + config.getServerId());
    }

    /**
     * Initializes all plugin components.
     */
    private void initializeComponents() {
        // Initialize messaging service
        messagingService = new MessagingService(config);

        // Initialize server manager first (needed by handlers)
        serverManager = new ServerManager(config, messagingService);

        // Initialize handler registry after server manager
        handlerRegistry = new HandlerRegistry(this);
        messagingService.setHandlerRegistry(handlerRegistry);

        // Initialize event listeners
        playerEventListener = new PlayerEventListener(this);
        serverEventListener = new ServerEventListener(this);
        chatSyncListener = new ChatSyncListener(this);

        // Register built-in handlers
        registerBuiltInHandlers();

        LoggerUtil.info("Plugin components initialized");
    }

    /**
     * Registers event listeners.
     */
    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(playerEventListener, this);
        getServer().getPluginManager().registerEvents(serverEventListener, this);

        // Only register chat sync listener if chat sync is enabled
        if (config.isChatSyncEnabled()) {
            getServer().getPluginManager().registerEvents(chatSyncListener, this);
            LoggerUtil.info("Chat synchronization enabled");
        } else {
            LoggerUtil.info("Chat synchronization disabled in configuration");
        }

        LoggerUtil.info("Event listeners registered");
    }

    /**
     * Registers built-in message handlers.
     */
    private void registerBuiltInHandlers() {
        // Register core system handlers
        if (config.isTestCommandsEnabled()) {
            handlerRegistry.registerHandler(new TestMessageHandler());
            LoggerUtil.info("Test commands enabled - TestMessageHandler registered");
        }
        handlerRegistry.registerHandler(new ServerManagerHandler());

        // Register feature handlers only if enabled
        if (config.isChatSyncEnabled()) {
            handlerRegistry.registerHandler(new ChatSyncHandler());
            LoggerUtil.info("Chat sync handler registered");
        }

        // Register example custom handler (for demonstration)
        handlerRegistry.registerHandler(new ExampleCustomHandler());

        LoggerUtil.info("Built-in handlers registered");
    }

    /**
     * Registers plugin commands.
     */
    private void registerCommands() {
        ChapeauCommand chapeauCommand = new ChapeauCommand(this);

        PluginCommand command = getCommand(config.getCommandPrefix());
        if (command != null) {
            command.setExecutor(chapeauCommand);
            command.setTabCompleter(chapeauCommand);
        } else {
            LoggerUtil.warning("Failed to register '" + config.getCommandPrefix() + "' command - command not found in plugin.yml");
        }

        LoggerUtil.info("Commands registered");
    }

    /**
     * Starts all plugin services.
     */
    private void startServices() {
        // Start messaging service
        if (messagingService.start()) {
            LoggerUtil.info("Messaging service started successfully");
        } else {
            LoggerUtil.warning("Failed to start messaging service");
        }

        // Start server manager
        serverManager.start();

        LoggerUtil.info("All services started");
    }

    /**
     * Stops all plugin services.
     */
    private void stopServices() {
        if (serverManager != null) {
            serverManager.stop();
        }

        if (messagingService != null) {
            messagingService.stop();
        }

        LoggerUtil.info("All services stopped");
    }

    /**
     * Gets the plugin instance.
     *
     * @return The plugin instance
     */
    public static ChapeauPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the Chapeau plugin configuration.
     *
     * @return The configuration instance
     */
    public ChapeauConfig getChapeauConfig() {
        return config;
    }

    /**
     * Gets the messaging service.
     *
     * @return The messaging service instance
     */
    public MessagingService getMessagingService() {
        return messagingService;
    }

    /**
     * Gets the server manager.
     *
     * @return The server manager instance
     */
    public ServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Gets the handler registry for registering custom message handlers.
     *
     * @return The handler registry instance
     */
    public HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }
}
