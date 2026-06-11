package com.chatapp.server.cluster;

import com.chatapp.shared.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Physical transport link between server nodes.
 *
 * <p>Accepts a {@link Message} and a target {@code nodeId}, then forwards the
 * message over the inter-server channel (TCP / WebSocket / gRPC — implementation
 * is swappable behind this interface).
 *
 * <p>In the Alice-to-Bob cross-server scenario:
 * <pre>
 *   Server1.MessageRouter
 *       → ServerRegistry.findServer("bob") = "server-2"
 *       → InterServerConnection.relay(message, "server-2")
 *           → [TCP/gRPC] →
 *               Server2.ConnectionManager.deliverLocal(message)
 *                   → Bob's ClientSession.send(message)
 * </pre>
 */
public final class InterServerConnection {

    private static final Logger log = LoggerFactory.getLogger(InterServerConnection.class);

    /**
     * Relay a message to a specific remote server node.
     *
     * @param message the message to forward
     * @param targetNodeId the ID of the destination server node
     */
    public void relay(Message message, String targetNodeId) {
        log.debug("Relaying message '{}' to node '{}'", message.getId(), targetNodeId);

        // TODO: look up the target node's address from ClusterManager
        // TODO: obtain / reuse a persistent channel to that node
        // TODO: encode message and write to the inter-server channel
        //
        // Example with gRPC:
        //   ClusterServiceGrpc.ClusterServiceBlockingStub stub = stubs.get(targetNodeId);
        //   stub.relay(GrpcMessageMapper.toProto(message));
    }
}
