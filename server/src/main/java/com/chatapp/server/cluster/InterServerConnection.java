package com.chatapp.server.cluster;

import com.chatapp.shared.model.Message;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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

    private final Set<String> connectedNodes = ConcurrentHashMap.newKeySet();

    private volatile BiConsumer<String, Message> incomingHandler;

    /** The gRPC server that listens for incoming relay calls from peer nodes. */
    private Server grpcServer;

    public void start() throws IOException {
        log.info("Starting inter-server connection");

        // bind a server port (e.g., 50001) to receive messages from other nodes
        // implement the server-side gRPC service (ClusterService)
        // expose the `relay` method as an RPC
        // register discovered peer nodes in `connectedNodes`
        // start a background task that attempts to connect to newly discovered peers
        // set `this.incomingHandler` to the central MessageRouter
        grpcServer = ServerBuilder.forPort(50001)
                .addService(new ClusterServiceImpl())
                .build()
                .start();

        log.info("Inter-server gRPC listener started on port 50001");
    }

    /** Gracefully shuts down the gRPC listener. */
    public void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            log.info("Inter-server gRPC listener stopped");
        }
    }

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
