package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
/**
 * {@code WebSocketDataReader} is an implementation of the {@link DataReader} interface
 * that connects to a WebSocket server and reads real-time patient data.
 * <p>
 * This class listens for incoming WebSocket messages, parses each message,
 * and stores the data using a {@link DataStorage} instance.
 * </p>
 *
 * <h2>Expected Message Format</h2>
 * <pre>{@code
 * patientId,timestamp,label,value
 * }</pre>
 * <h2>Error Handling</h2>
 * <ul>
 *     <li>If the connection to the server fails or exceeds the specified timeout, an {@link IOException} is thrown.</li>
 *     <li>Malformed messages (e.g., wrong number of fields or invalid number format) are logged but do not crash the application.</li>
 * </ul>
 */
public class WebSocketDataReader implements DataReader {
    private final WebSocketClient client;
    private final int connectionTimeout;
    /**
     * Constructs a new {@code WebSocketDataReader} with a default connection timeout of 5000 milliseconds.
     *
     * @param serverUri the URI of the WebSocket server (e.g., {@code "ws://localhost:8080/data"})
     * @throws IllegalArgumentException if the provided URI is invalid
     */
    public WebSocketDataReader(String serverUri) {
        this(serverUri, 5000); // Default 5s timeout
    }
    /**
     * Constructs a new {@code WebSocketDataReader} with a custom connection timeout.
     * @param serverUri         the URI of the WebSocket server
     * @param connectionTimeout the connection timeout in milliseconds
     * @throws IllegalArgumentException if the provided URI is invalid
     */
    public WebSocketDataReader(String serverUri, int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.client = createClient(serverUri);
    }
    /**
     * Creates a WebSocket client that handles connection events and incoming messages
     * @param serverUri the URI of the WebSocket server
     * @return a configured {@link WebSocketClient} instance
     * @throws IllegalArgumentException if the provided URI is syntactically invalid
     */
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
    /**
     * Initiates the connection to the WebSocket server and waits until the connection is established
     * @param dataStorage the storage to which incoming data will be saved (not directly used here)
     * @throws IOException if the connection cannot be established within the configured timeout
     */
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
                Thread.sleep(100); // Pause between the connections
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Connection interrupted", e);
            }
        }
    }

    @Override
    public void stopReading() {
        client.close();
    } //Stops the WebSocket client and closes the connection to the server.
    /**
     * The message here is expected to be a comma-separated string with four values:
     * {@code patientId,timestamp,label,value}.
     * <p>If the message is malformed or any value cannot be parsed, an error is logged.</p>
     * @param message the message received from the server
     */
    protected void processMessage(String message) { //Parses and processes a message received from the WebSocket server.
        try {
            String[] parts = message.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid message format");
            }
            // Parsing of the components in a message
            int patientId = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            String label = parts[2];
            double value = Double.parseDouble(parts[3]);
            // Saving all the data
            DataStorage.getInstance().addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing message: " + message);
        }
    }
}