package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
/**
 * A mock WebSocket server is used for testing.
 * This server simulates a WebSocket endpoint that can accept connections,
 * broadcast messages to all connected clients, and track active connections.
 *
 * <p>Key features:
 * <ul>
 *   <li>Can manage client connections in a thread-safe collection</li>
 *   <li>Supports broadcasting messages to all connected clients</li>
 *   <li>Provides basic connection lifecycle callbacks</li>
 *   <li>Enables socket address reuse for rapid test cycles</li>
 * </ul>
 *
 * <p>Typical usage in testing:
 * <ol>
 *   <li>Instantiate server on a test port</li>
 *   <li>Connect test clients</li>
 *   <li>Broadcast test messages</li>
 *   <li>Verify client received messages</li>
 * </ol>
 */
public class MockWebSocketServer extends WebSocketServer {
    private final Collection<WebSocket> connections = new CopyOnWriteArraySet<>();
    /**
     * Creates a new mock WebSocket server bound to the specified port.
     * Enables SO_REUSEADDR to allow rapid restart of test servers.
     *
     * @param port the TCP port to bind to
     */
    public MockWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }
    /**
     * Broadcasts a message to all currently connected WebSocket clients.
     * The message will be delivered asynchronously to each connection.
     *
     * @param message the text message to broadcast
     */
    public void broadcast(String message) {
        connections.forEach(conn -> conn.send(message));
    }
    /**
     * Handles new WebSocket connections.
     * Adds the new connection to the active connections set.
     *
     * @param conn the new WebSocket connection
     * @param handshake the handshake data
     */
    @Override
    public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
        connections.add(conn);
    }
    /**
     * Handles WebSocket connection closures.
     * Removes the connection from the active connections set.
     *
     * @param conn the closed WebSocket connection
     * @param code the closure status code
     * @param reason the closure reason
     * @param remote whether the closure was initiated by the remote host
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
    }
    /**
     * Handles incoming WebSocket messages.
     * This implementation does nothing with received messages by default.
     * Override this method to add message processing logic for specific tests.
     *
     * @param conn the source WebSocket connection
     * @param message the received message content
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Optional: Add message processing logic if needed
    }
    /**
     * Handles WebSocket communication errors.
     * Logs the error to standard error output.
     *
     * @param conn the WebSocket connection where the error occurred (may be null)
     * @param ex the exception representing the error
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Server error: " + ex.getMessage());
    }
    /**
     * Called when the server successfully starts.
     * Logs the server's port to standard output.
     */
    @Override
    public void onStart() {
        System.out.println("Test server started on port " + getPort());
    }
}