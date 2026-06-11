package com.chatapp.server.connection;

import com.chatapp.shared.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single authenticated client connection to this server node.
 *
 * <p>Each {@code ClientSession} wraps the underlying transport channel (Netty
 * {@code Channel} in the real implementation) and exposes a clean {@link #send}
 * method that the rest of the server uses to push messages to the client without
 * coupling to networking internals.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Created by the Netty pipeline handler on connection &amp; auth success.</li>
 *   <li>Registered in {@link ConnectionManager}.</li>
 *   <li>Closed and deregistered on disconnect or auth failure.</li>
 * </ol>
 */
public final class ClientSession {

    private static final Logger log = LoggerFactory.getLogger(ClientSession.class);

    private final String userId;
    private final String sessionId;

    // TODO: replace with io.netty.channel.Channel
    private volatile boolean open = true;

    public ClientSession(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    /**
     * Push a message down the wire to this client.
     *
     * @param message the message to send
     * @throws IllegalStateException if the session has been closed
     */
    public void send(Message message) {
        if (!open) {
            throw new IllegalStateException(
                    "Cannot send to closed session for user '" + userId + "'");
        }
        // TODO: encode message and write to Netty channel
        log.debug("[{}] → client '{}': {}", sessionId, userId, message);
    }

    /** Close the underlying transport channel and mark this session as inactive. */
    public void close() {
        if (open) {
            open = false;
            // TODO: channel.close()
            log.info("Session '{}' closed for user '{}'", sessionId, userId);
        }
    }

    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public boolean isOpen() { return open; }

    @Override
    public String toString() {
        return "ClientSession{userId='" + userId + "', sessionId='" + sessionId + "', open=" + open + "}";
    }
}
