package com.chatapp.server.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks the health and availability of every server node in the cluster.
 *
 * <p>Each node registers itself on startup and sends periodic heartbeats.
 * Nodes that miss too many heartbeats are marked {@link NodeStatus#UNREACHABLE}
 * and excluded from routing decisions.
 *
 * <p><strong>This is NOT {@link com.chatapp.server.connection.ConnectionManager}.</strong>
 * ClusterManager knows about <em>server nodes</em>; ConnectionManager knows about
 * <em>client sessions</em> on the local node.
 */
public final class ClusterManager {

    private static final Logger log = LoggerFactory.getLogger(ClusterManager.class);

    private final ConcurrentMap<String, NodeInfo> nodes = new ConcurrentHashMap<>();

    /**
     * Register a server node (called when a node joins the cluster).
     *
     * @param nodeId  unique identifier for the node (e.g. hostname:port)
     * @param address network address used by {@link InterServerConnection}
     */
    public void registerNode(String nodeId, String address) {
        NodeInfo info = new NodeInfo(nodeId, address, NodeStatus.HEALTHY, Instant.now());
        nodes.put(nodeId, info);
        log.info("Node '{}' registered at '{}'", nodeId, address);
    }

    /**
     * Remove a node from the cluster view (called on graceful shutdown or eviction).
     *
     * @param nodeId the node to remove
     */
    public void deregisterNode(String nodeId) {
        nodes.remove(nodeId);
        log.info("Node '{}' deregistered", nodeId);
    }

    /**
     * Record a heartbeat from a node, updating its last-seen timestamp.
     *
     * @param nodeId the node that sent the heartbeat
     */
    public void heartbeat(String nodeId) {
        nodes.computeIfPresent(nodeId, (id, info) ->
                new NodeInfo(id, info.address(), NodeStatus.HEALTHY, Instant.now()));
    }

    /**
     * Mark a node as unreachable (e.g. after missed heartbeats).
     *
     * @param nodeId the node to mark
     */
    public void markUnreachable(String nodeId) {
        nodes.computeIfPresent(nodeId, (id, info) ->
                new NodeInfo(id, info.address(), NodeStatus.UNREACHABLE, info.lastSeen()));
        log.warn("Node '{}' marked as UNREACHABLE", nodeId);
    }

    /** @return all nodes currently known (healthy or otherwise) */
    public Collection<NodeInfo> allNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /** @return only nodes currently considered healthy */
    public Collection<NodeInfo> healthyNodes() {
        return nodes.values().stream()
                .filter(n -> n.status() == NodeStatus.HEALTHY)
                .toList();
    }

    /**
     * Simple value type describing a cluster node.
     *
     * @param nodeId   unique node identifier
     * @param address  network address (host:port)
     * @param status   current health status
     * @param lastSeen timestamp of the most recent heartbeat
     */
    public record NodeInfo(String nodeId, String address, NodeStatus status, Instant lastSeen) {}
}
