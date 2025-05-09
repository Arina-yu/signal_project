package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketDataReader implements DataReader {
    private final WebSocketClient client;
    private final int connectionTimeout;

    public WebSocketDataReader(String serverUri) {
        this(serverUri, 5000); // Default 5s timeout
    }

    public WebSocketDataReader(String serverUri, int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.client = createClient(serverUri);
    }

    private WebSocketClient createClient(String serverUri) {
        try {
            return new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server");
                }

                @Override
                public void onMessage(String message) {
                    processMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                }
            };
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI", e);
        }
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        client.connect();

        // Wait for connection with timeout
        long startTime = System.currentTimeMillis();
        while (!client.isOpen()) {
            if (System.currentTimeMillis() - startTime > connectionTimeout) {
                throw new IOException("Connection failed to open within timeout");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Connection interrupted", e);
            }
        }
    }

    @Override
    public void stopReading() {
        client.close();
    }

    protected void processMessage(String message) {
        try {
            String[] parts = message.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid message format");
            }

            int patientId = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            String label = parts[2];
            double value = Double.parseDouble(parts[3]);

            DataStorage.getInstance().addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing message: " + message);
        }
    }
}