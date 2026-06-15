package com.chatapp.server.cluster;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side gRPC implementation of the cluster relay service.
 *
 * <p>Receives inbound {@code relay} RPCs from peer server nodes and dispatches
 * the payload to the local {@code MessageRouter} via the {@code incomingHandler}
 * registered by {@link InterServerConnection}.
 *
 * <p>This is a placeholder until the protobuf-generated service base class
 * ({@code ClusterServiceGrpc.ClusterServiceImplBase}) is available.
 *
 * <p>TODO: extend {@code ClusterServiceGrpc.ClusterServiceImplBase} once
 *          {@code cluster_service.proto} is compiled.
 * <p>TODO: inject the {@code incomingHandler} (BiConsumer) so received messages
 *          are forwarded to the local MessageRouter.
 */
public class ClusterServiceImpl extends io.grpc.BindableService {

    private static final Logger log = LoggerFactory.getLogger(ClusterServiceImpl.class);

    /**
     * {@inheritDoc}
     *
     * <p>Returns the server service definition for this gRPC service.
     * Replace this stub with the generated base class once the proto is compiled.
     */
    @Override
    public io.grpc.ServerServiceDefinition bindService() {
        // TODO: replace with ClusterServiceGrpc.ClusterServiceImplBase once
        //       cluster_service.proto is compiled and generated sources are available.
        log.warn("ClusterServiceImpl.bindService() called on stub — proto not yet compiled");
        return io.grpc.ServerServiceDefinition.builder("ClusterService").build();
    }
}
