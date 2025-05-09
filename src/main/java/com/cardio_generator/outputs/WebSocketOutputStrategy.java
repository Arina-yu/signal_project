package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collection;

public class WebSocketOutputStrategy implements OutputStrategy {

    private static WebSocketServer server;

    public WebSocketOutputStrategy(int port) {
        this(createDefaultServer(port));
    }

    // Package-private constructor for testing
    WebSocketOutputStrategy(WebSocketServer server) {
        this.server = server;
        System.out.println("WebSocket server created on port: " + server.getPort() + ", listening for connections...");
        server.start();
    }

    public WebSocketServer getServer() {
        return server;
    }

    public static WebSocketServer createDefaultServer(int port) {
        return new SimpleWebSocketServer(new InetSocketAddress(port));
    }

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
        System.out.println("Broadcasting message: " + message);
    }

    private static class SimpleWebSocketServer extends WebSocketServer {
        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used in this context
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }

        @Override
        public Collection<WebSocket> getConnections() {
            return super.getConnections();
        }
    }
}