package com.chatapp.server.cluster;

/** Health status of a node as perceived by the {@link ClusterManager}. */
public enum NodeStatus {
    /** Node is responsive and accepting connections. */
    HEALTHY,
    /** Node is suspected down (missed heartbeats) but not yet evicted. */
    SUSPECT,
    /** Node is confirmed unreachable; excluded from routing. */
    UNREACHABLE
}
