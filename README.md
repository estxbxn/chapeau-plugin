# chapeau-plugin

A professional Minecraft multi-server communication plugin that uses RabbitMQ for reliable inter-server messaging. This plugin is designed to facilitate testing and management of multi-server setups by providing real-time communication, monitoring, and synchronization between different Minecraft servers.

## Requirements

- Minecraft Server 1.21.4+ (Paper/Spigot)
- Java 21+
- RabbitMQ Server

## Installation

1. Install RabbitMQ server (see [RabbitMQ Installation Guide](https://www.rabbitmq.com/download.html) or use Docker):
   ```bash
   docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```
2. Build the plugin using Gradle: `./gradlew build`
3. Place the generated JAR file in each server's `plugins` folder
4. Configure each server with unique settings (see Configuration section)
5. Start your servers

## Configuration

### Multi-Server Setup Example

For a typical multi-server setup, each server needs unique identification:

**Game Server 01** (`config.yml`):
```yaml
server:
  id: "server01"
  name: "Game Server 01"
features:
  chatSync:
    enabled: true
    format: "[{server}] <{player}> {message}"
commands:
  enableTestCommands: true
```

**Game Server 02** (`config.yml`):
```yaml
server:
  id: "server02"
  name: "Game Server 02"
features:
  chatSync:
    enabled: true
    format: "[{server}] <{player}> {message}"
commands:
  enableTestCommands: true
```

## Commands

All commands require appropriate permissions (`chapeau.admin` or `chapeau.test`). Commands can be disabled via configuration.

### Administrative Commands

- `/chapeau status` - Show plugin and server status
- `/chapeau servers` - List all known servers and their status  
- `/chapeau handlers` - Show registered message handlers
- `/chapeau reload` - Reload the plugin configuration

### Test Commands (can be disabled in config)

- `/chapeau test` - Send a test broadcast message
- `/chapeau send <server> <message>` - Send a message to a specific server
- `/chapeau broadcast <message>` - Broadcast a message to all servers
- `/chapeau custom <server> <action> [data]` - Send custom messages with actions

### Custom Command Actions

The custom command supports various actions for advanced testing:

- `ping` - Send a ping request to a server
- `player_count` - Request player count from a server
- `execute_command <command>` - Execute a safe command on remote server
- `server_info` - Get detailed server information

### Examples

```bash
# Basic commands
/chapeau status
/chapeau servers
/chapeau handlers

# Test commands (if enabled)
/chapeau test
/chapeau send server02 Hello from server01!
/chapeau broadcast System maintenance in 5 minutes

# Custom commands
/chapeau custom server02 ping
/chapeau custom server02 player_count
/chapeau custom server02 execute_command say Hello from server01!
/chapeau custom server02 server_info
```

## Permissions

- `chapeau.admin` - Full access to all commands (default: op)
- `chapeau.test` - Access to test commands (default: op)

### Message Handler System

The plugin uses an extensible handler architecture:

- **ChapeauMessageHandler**: Base interface for message processing
- **MultiTypeMessageHandler**: Interface for handlers processing multiple message types  
- **HandlerContext**: Provides utilities and services to handlers
- **Built-in Handlers**: TestMessageHandler, ServerManagerHandler, ChatSyncHandler, ExampleCustomHandler

### Message Types

- `HEARTBEAT` - Server health monitoring with automatic cleanup
- `STATUS_UPDATE` - Server status and player count changes
- `PLAYER_SYNC` - Player join/quit events across servers
- `CHAT_BROADCAST` - Cross-server chat synchronization
- `TEST_MESSAGE` - Testing communication (can be disabled)
- `SERVER_STARTUP/SHUTDOWN` - Server lifecycle events
- `CUSTOM` - Extensible custom message types

## Testing

### Setting up a Test Environment

1. Install RabbitMQ locally or use Docker:
   ```bash
   docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

2. Start multiple Minecraft servers with different configurations
3. Use the plugin commands to test communication between servers

### Debug Mode

Enable comprehensive debug logging in `config.yml`:
```yaml
messaging:
  debug: true           # Enables detailed message logging
```

This will log:
- All sent and received messages
- Handler execution details
- Connection status changes
- Configuration loading information

## Advanced Features

### Custom Message Handlers

The plugin supports custom message handlers for extended functionality:

```java
public class MyCustomHandler implements ChapeauMessageHandler {
    @Override
    public String getMessageType() {
        return "my_custom_type"; // Or add to MessageType enum your custom type
    }
    
    @Override
    public boolean handleMessage(Message message, HandlerContext context) {
        // Process your custom message
        return true;
    }
    
    @Override
    public int getPriority() {
        return 50; // Higher priority handlers execute first
    }
}

// Register in your plugin
plugin.getHandlerRegistry().registerHandler(new MyCustomHandler());
```

The plugin is designed with extensibility in mind, making it easy to add new features while maintaining compatibility.
