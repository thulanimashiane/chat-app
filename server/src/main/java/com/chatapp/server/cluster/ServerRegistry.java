package com.chatapp.server.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains the global {@code userId → nodeId} mapping so the
 * {@link com.chatapp.server.routing.MessageRouter} can find which server node
 * a user is connected to.
 *
 * <h3>Typical flow</h3>
 * <ol>
 *   <li>User connects to Server 2 → Server 2 calls {@link #register}.</li>
 *   <li>User on Server 1 sends a message → {@link com.chatapp.server.routing.MessageRouter}
 *       calls {@link #findServer} to discover Server 2.</li>
 *   <li>Message is relayed to Server 2 via {@link InterServerConnection}.</li>
 *   <li>User disconnects → Server 2 calls {@link #deregister}.</li>
 * </ol>
 *
 * <p>In production this registry would be backed by a distributed store
 * (e.g. Redis) so all nodes share the same view.
 */
public final class ServerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ServerRegistry.class);

    /** userId → nodeId */
    private final ConcurrentMap<String, String> userLocation = new ConcurrentHashMap<>();

    /**
     * Record that {@code userId} is connected to {@code nodeId}.
     *
     * @param userId the authenticated user
     * @param nodeId the node they connected to
     */
    public void register(String userId, String nodeId) {
        userLocation.put(userId, nodeId);
        log.debug("Registered user '{}' on node '{}'", userId, nodeId);
    }

    /**
     * Remove the mapping when a user disconnects.
     *
     * @param userId the disconnecting user
     */
    public void deregister(String userId) {
        String removed = userLocation.remove(userId);
        if (removed != null) {
            log.debug("Deregistered user '{}' from node '{}'", userId, removed);
        }
    }

    /**
     * Find which node a user is currently connected to.
     *
     * @param userId the user to locate
     * @return the node ID, or {@link Optional#empty()} if the user is offline
     */
    public Optional<String> findServer(String userId) {
        return Optional.ofNullable(userLocation.get(userId));
    }

    /** @return {@code true} if the user has a registered location (is online somewhere) */
    public boolean isOnline(String userId) {
        return userLocation.containsKey(userId);
    }
}
