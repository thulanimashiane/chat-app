package com.chatapp.server.routing;

import com.chatapp.server.cluster.InterServerConnection;
import com.chatapp.server.cluster.ServerRegistry;
import com.chatapp.server.connection.ConnectionManager;
import com.chatapp.shared.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Central routing brain for outbound messages on this server node.
 *
 * <h3>Routing algorithm</h3>
 * <ol>
 *   <li>Is the recipient connected <em>locally</em>?
 *       → deliver via {@link ConnectionManager#deliverLocal}.</li>
 *   <li>Is the recipient on a <em>remote</em> node (known to {@link ServerRegistry})?
 *       → hand off to {@link InterServerConnection} for forwarding.</li>
 *   <li>Recipient unknown → queue message for offline delivery (push / email).</li>
 * </ol>
 *
 * <p>The MessageRouter does <strong>not</strong> maintain its own state; it is a
 * pure coordinator that delegates to the three components above.
 */
public final class MessageRouter {

    private static final Logger log = LoggerFactory.getLogger(MessageRouter.class);

    private final ConnectionManager connectionManager;
    private final ServerRegistry serverRegistry;
    private final InterServerConnection interServerConnection;

    public MessageRouter(
            ConnectionManager connectionManager,
            ServerRegistry serverRegistry,
            InterServerConnection interServerConnection) {
        this.connectionManager = connectionManager;
        this.serverRegistry = serverRegistry;
        this.interServerConnection = interServerConnection;
    }

    /**
     * Route a message to its intended recipient using the algorithm described above.
     *
     * @param message the message to route
     */
    public void route(Message message) {
        String recipientId = message.getRecipientId();
        log.debug("Routing message {} to '{}'", message.getId(), recipientId);

        // Step 1 — try local delivery
        if (connectionManager.isLocallyConnected(recipientId)) {
            log.debug("Recipient '{}' is local — delivering directly", recipientId);
            connectionManager.deliverLocal(message);
            return;
        }

        // Step 2 — try cluster relay
        Optional<String> remoteNode = serverRegistry.findServer(recipientId);
        if (remoteNode.isPresent()) {
            log.debug("Recipient '{}' is on node '{}' — relaying via cluster",
                    recipientId, remoteNode.get());
            interServerConnection.relay(message, remoteNode.get());
            return;
        }

        // Step 3 — offline: hand off to notification service
        log.info("Recipient '{}' is offline — queueing for offline delivery", recipientId);
        // TODO: publish to offline-delivery queue / notification module
    }
}
