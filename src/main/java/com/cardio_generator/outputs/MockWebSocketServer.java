package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class MockWebSocketServer extends WebSocketServer {
    private final Collection<WebSocket> connections = new CopyOnWriteArraySet<>();

    public MockWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }

    public void broadcast(String message) {
        connections.forEach(conn -> conn.send(message));
    }

    @Override
    public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
        connections.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Optional: Add message processing logic if needed
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Server error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Test server started on port " + getPort());
    }
}