package com.whimsy.chapeau.commands;

import com.whimsy.chapeau.ChapeauPlugin;
import com.whimsy.chapeau.messaging.MessageType;
import com.whimsy.chapeau.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Main command handler for the Chapeau plugin.
 * Provides subcommands for testing multi-server communication and managing the plugin.
 *
 * @author Whimsy
 * @version 1.0.0
 */
public class ChapeauCommand implements CommandExecutor, TabCompleter {

    private final ChapeauPlugin plugin;

    /**
     * Creates a new ChapeauCommand instance.
     *
     * @param plugin The plugin instance
     */
    public ChapeauCommand(ChapeauPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "status":
                return handleStatusCommand(sender, args);
            case "servers":
                return handleServersCommand(sender, args);
            case "test":
                return handleTestCommand(sender, args);
            case "send":
                return handleSendCommand(sender, args);
            case "broadcast":
                return handleBroadcastCommand(sender, args);
            case "handlers":
                return handleHandlersCommand(sender, args);
            case "custom":
                return handleCustomCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(Component.text("Unknown subcommand: " + subCommand)
                        .color(NamedTextColor.RED));
                sendHelpMessage(sender);
                return true;
        }
    }

    /**
     * Handles the status subcommand.
     */
    private boolean handleStatusCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        ServerInfo currentServer = plugin.getServerManager().getCurrentServerInfo();
        boolean messagingStarted = plugin.getMessagingService().isStarted();
        boolean connected = plugin.getMessagingService().getConnectionManager().isConnected();

        sender.sendMessage(Component.text("=== Chapeau Plugin Status ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        sender.sendMessage(Component.text("Server ID: " + currentServer.getServerId())
                .color(NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Server Name: " + currentServer.getServerName())
                .color(NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Server Type: " + currentServer.getServerType())
                .color(NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Player Count: " + currentServer.getPlayerCount())
                .color(NamedTextColor.AQUA));

        sender.sendMessage(Component.text("Messaging Service: " + (messagingStarted ? "Started" : "Stopped"))
                .color(messagingStarted ? NamedTextColor.GREEN : NamedTextColor.RED));
        sender.sendMessage(Component.text("RabbitMQ Connection: " + (connected ? "Connected" : "Disconnected"))
                .color(connected ? NamedTextColor.GREEN : NamedTextColor.RED));

        return true;
    }

    /**
     * Handles the servers subcommand.
     */
    private boolean handleServersCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        Map<String, ServerInfo> servers = plugin.getServerManager().getAllServers();
        List<ServerInfo> onlineServers = plugin.getServerManager().getOnlineServers();

        sender.sendMessage(Component.text("=== Server List ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        sender.sendMessage(Component.text("Total servers: " + servers.size() + " | Online: " + onlineServers.size())
                .color(NamedTextColor.AQUA));

        if (servers.isEmpty()) {
            sender.sendMessage(Component.text("No servers found.")
                    .color(NamedTextColor.YELLOW));
            return true;
        }

        for (ServerInfo server : servers.values()) {
            boolean isOnline = server.isOnline(120000L); // 2 minutes timeout
            NamedTextColor statusColor = isOnline ? NamedTextColor.GREEN : NamedTextColor.RED;
            String statusText = isOnline ? "ONLINE" : "OFFLINE";

            sender.sendMessage(Component.text("• " + server.getServerId() + " (" + server.getServerName() + ")")
                    .color(NamedTextColor.WHITE)
                    .append(Component.text(" - " + statusText)
                            .color(statusColor))
                    .append(Component.text(" - " + server.getServerType())
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(" - Players: " + server.getPlayerCount())
                            .color(NamedTextColor.GRAY)));
        }

        return true;
    }

    /**
     * Handles the test subcommand.
     */
    private boolean handleTestCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.test")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getChapeauConfig().isTestCommandsEnabled()) {
            sender.sendMessage(Component.text("Test commands are disabled in configuration.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getMessagingService().isStarted()) {
            sender.sendMessage(Component.text("Messaging service is not started.")
                    .color(NamedTextColor.RED));
            return true;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Test message from " + plugin.getChapeauConfig().getServerId());
        data.put("sender", sender.getName());
        data.put("timestamp", System.currentTimeMillis());

        boolean success = plugin.getMessagingService().sendBroadcast(MessageType.TEST_MESSAGE.getTypeName(), data);

        if (success) {
            sender.sendMessage(Component.text("Test message sent successfully!")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to send test message.")
                    .color(NamedTextColor.RED));
        }

        return true;
    }

    /**
     * Handles the send subcommand.
     */
    private boolean handleSendCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.test")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getChapeauConfig().isTestCommandsEnabled()) {
            sender.sendMessage(Component.text("Test commands are disabled in configuration.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /" + plugin.getChapeauConfig().getCommandPrefix() + " send <server> <message>")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getMessagingService().isStarted()) {
            sender.sendMessage(Component.text("Messaging service is not started.")
                    .color(NamedTextColor.RED));
            return true;
        }

        String targetServer = args[1];
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("sender", sender.getName());
        data.put("timestamp", System.currentTimeMillis());

        boolean success = plugin.getMessagingService().sendToServer(targetServer, MessageType.TEST_MESSAGE.getTypeName(), data);

        if (success) {
            sender.sendMessage(Component.text("Message sent to " + targetServer + ": " + message)
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to send message to " + targetServer)
                    .color(NamedTextColor.RED));
        }

        return true;
    }

    /**
     * Handles the broadcast subcommand.
     */
    private boolean handleBroadcastCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /" + plugin.getChapeauConfig().getCommandPrefix() + " broadcast <message>")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getMessagingService().isStarted()) {
            sender.sendMessage(Component.text("Messaging service is not started.")
                    .color(NamedTextColor.RED));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("sender", sender.getName());
        data.put("timestamp", System.currentTimeMillis());

        boolean success = plugin.getMessagingService().sendBroadcast(MessageType.TEST_MESSAGE.getTypeName(), data);

        if (success) {
            sender.sendMessage(Component.text("Broadcast message sent: " + message)
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to send broadcast message.")
                    .color(NamedTextColor.RED));
        }

        return true;
    }

    /**
     * Handles the handlers subcommand.
     */
    private boolean handleHandlersCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        var handlerRegistry = plugin.getHandlerRegistry();
        if (handlerRegistry == null) {
            sender.sendMessage(Component.text("Handler registry not available.")
                    .color(NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("=== Registered Handlers ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        var handlerInfo = handlerRegistry.getHandlerInfo();
        int totalHandlers = handlerRegistry.getTotalHandlerCount();

        sender.sendMessage(Component.text("Total handlers: " + totalHandlers)
                .color(NamedTextColor.AQUA));

        if (handlerInfo.isEmpty()) {
            sender.sendMessage(Component.text("No handlers registered.")
                    .color(NamedTextColor.YELLOW));
        } else {
            for (var entry : handlerInfo.entrySet()) {
                sender.sendMessage(Component.text("• " + entry.getKey() + ": " + entry.getValue() + " handler(s)")
                        .color(NamedTextColor.WHITE));
            }
        }

        return true;
    }

    /**
     * Handles the custom subcommand for testing custom handlers.
     */
    private boolean handleCustomCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.test")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!plugin.getChapeauConfig().isTestCommandsEnabled()) {
            sender.sendMessage(Component.text("Test commands are disabled in configuration.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 4) {
            String prefix = plugin.getChapeauConfig().getCommandPrefix();
            sender.sendMessage(Component.text("Usage: /" + prefix + " custom <server> <action> <data...>")
                    .color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Examples:")
                    .color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /" + prefix + " custom server02 ping")
                    .color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /" + prefix + " custom server02 player_count")
                    .color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /" + prefix + " custom server02 execute_command say Hello!")
                    .color(NamedTextColor.GRAY));
            return true;
        }

        if (!plugin.getMessagingService().isStarted()) {
            sender.sendMessage(Component.text("Messaging service is not started.")
                    .color(NamedTextColor.RED));
            return true;
        }

        String targetServer = args[1];
        String action = args[2];
        String additionalData = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        Map<String, Object> data = new HashMap<>();
        data.put("action", action);
        data.put("requester", sender.getName());
        data.put("timestamp", System.currentTimeMillis());

        // Add action-specific data
        if ("execute_command".equals(action) && !additionalData.isEmpty()) {
            data.put("command", additionalData);
        } else if (!additionalData.isEmpty()) {
            data.put("additional", additionalData);
        }

        boolean success = plugin.getMessagingService().sendToServer(targetServer, "example_custom", data);

        if (success) {
            sender.sendMessage(Component.text("Custom message sent to " + targetServer + " (action: " + action + ")")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to send custom message to " + targetServer)
                    .color(NamedTextColor.RED));
        }

        return true;
    }

    /**
     * Handles the reload subcommand.
     */
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chapeau.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Reloading Chapeau plugin...")
                .color(NamedTextColor.YELLOW));

        // Reload asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.reloadPlugin();
            sender.sendMessage(Component.text("Chapeau plugin reloaded successfully!")
                    .color(NamedTextColor.GREEN));
        });

        return true;
    }

    /**
     * Sends the help message to the command sender.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("=== Chapeau Commands ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        sender.sendMessage(Component.text("/chapeau status")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Show plugin status")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau servers")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - List all servers")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau test")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Send a test broadcast message")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau send <server> <message>")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Send message to specific server")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau broadcast <message>")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Broadcast message to all servers")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau handlers")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - List registered message handlers")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau custom <server> <action> [data]")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Send custom message")
                        .color(NamedTextColor.WHITE)));

        sender.sendMessage(Component.text("/chapeau reload")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Reload the plugin")
                        .color(NamedTextColor.WHITE)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("status", "servers", "test", "send", "broadcast", "handlers", "custom", "reload", "help");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("custom"))) {
            // Tab complete server names for send and custom commands
            Map<String, ServerInfo> servers = plugin.getServerManager().getAllServers();
            for (String serverId : servers.keySet()) {
                if (serverId.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(serverId);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("custom")) {
            // Tab complete actions for custom command
            List<String> actions = Arrays.asList("ping", "player_count", "execute_command", "server_info");
            for (String action : actions) {
                if (action.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(action);
                }
            }
        }

        return completions;
    }
}
