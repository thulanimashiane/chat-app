package com.chatapp.server.connection;

import com.chatapp.shared.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Owns every active {@link ClientSession} on <em>this</em> server instance.
 *
 * <p><strong>Scope:</strong> per-server, not per-cluster. The ConnectionManager has no
 * knowledge of users connected to other nodes — cross-server awareness is handled by
 * the cluster layer ({@link com.chatapp.server.cluster.ServerRegistry}).
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Register / deregister sessions when clients connect or disconnect.</li>
 *   <li>Provide fast session lookup by {@code userId}.</li>
 *   <li>Deliver an outbound {@link Message} to a locally-connected recipient.</li>
 * </ul>
 */
public final class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    /** userId → active session for every client on this node. */
    private final ConcurrentMap<String, ClientSession> sessions = new ConcurrentHashMap<>();

    /**
     * Register a newly established client session.
     *
     * @param session the session to register; must not be {@code null}
     */
    public void register(ClientSession session) {
        sessions.put(session.getUserId(), session);
        log.info("Registered session for user '{}'", session.getUserId());
    }

    /**
     * Remove a session when the client disconnects.
     *
     * @param userId the user whose session is ending
     */
    public void deregister(String userId) {
        ClientSession removed = sessions.remove(userId);
        if (removed != null) {
            log.info("Deregistered session for user '{}'", userId);
        }
    }

    /**
     * Look up the session for {@code userId} on this node.
     *
     * @param userId the target user
     * @return the {@link ClientSession}, or {@code null} if the user is not local
     */
    public ClientSession getSession(String userId) {
        return sessions.get(userId);
    }

    /**
     * @return {@code true} if {@code userId} has an active session on this node
     */
    public boolean isLocallyConnected(String userId) {
        return sessions.containsKey(userId);
    }

    /**
     * Deliver a message to a locally-connected recipient.
     *
     * @param message the message to deliver
     * @return {@code true} if delivery succeeded; {@code false} if the recipient is
     *         not connected to this node
     */
    public boolean deliverLocal(Message message) {
        ClientSession session = sessions.get(message.getRecipientId());
        if (session == null) {
            log.debug("User '{}' is not local — cannot deliver directly", message.getRecipientId());
            return false;
        }
        session.send(message);
        return true;
    }

    /** @return an unmodifiable view of all locally-connected user IDs */
    public Collection<String> localUserIds() {
        return Collections.unmodifiableSet(sessions.keySet());
    }

    /** @return the number of clients currently connected to this node */
    public int localConnectionCount() {
        return sessions.size();
    }
}
