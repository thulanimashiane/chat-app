package com.chatapp.shared.codec;

import com.chatapp.shared.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Serialises and deserialises {@link Message} objects to/from JSON.
 *
 * <p>Uses a singleton {@link ObjectMapper} configured for Java 8 time types.
 */
public final class MessageCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MessageCodec() {}

    /** Encode a {@link Message} to a UTF-8 JSON string. */
    public static String encode(Message message) {
        try {
            return MAPPER.writeValueAsString(message);
        } catch (Exception e) {
            throw new CodecException("Failed to encode message: " + message, e);
        }
    }

    /** Decode a UTF-8 JSON string back to a {@link Message}. */
    public static Message decode(String json) {
        try {
            return MAPPER.readValue(json, Message.class);
        } catch (Exception e) {
            throw new CodecException("Failed to decode message JSON: " + json, e);
        }
    }

    /** Unchecked wrapper for codec failures so callers do not need checked exceptions. */
    public static final class CodecException extends RuntimeException {
        public CodecException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
