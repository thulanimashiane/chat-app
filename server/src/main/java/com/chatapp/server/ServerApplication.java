package com.chatapp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the chat server process.
 *
 * <p>Wires together the {@link com.chatapp.server.connection.ConnectionManager},
 * {@link com.chatapp.server.routing.MessageRouter}, and cluster layer, then
 * starts accepting client connections.
 */
public final class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting Chat Server...");

        // TODO: read port from config / env
        int port = 8080;

        // TODO: build and wire components
        // ConnectionManager connectionManager = new ConnectionManager();
        // MessageRouter router = new MessageRouter(connectionManager, ...);
        // ChatServer server = new ChatServer(port, connectionManager, router);
        // server.start();

        log.info("Chat Server listening on port {}", port);

        // Block the main thread
        Thread.currentThread().join();
    }
}
