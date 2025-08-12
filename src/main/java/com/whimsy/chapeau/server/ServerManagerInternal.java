package com.whimsy.chapeau.server;

/**
 * Internal interface for ServerManager to allow adding servers.
 * This interface should be implemented by ServerManager.
 */
public interface ServerManagerInternal {
    void addServer(ServerInfo server);
}