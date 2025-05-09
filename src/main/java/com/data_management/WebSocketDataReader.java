package com.data_management;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket client for receiving real-time patient data.
 * Parses incoming messages and stores them in DataStorage.
 */
public class WebSocketDataReader implements DataReader {
    private final DataStorage dataStorage;
    private WebSocketClient client;

    public WebSocketDataReader(String serverUri) {
        this.dataStorage = DataStorage.getInstance();
        initializeClient(serverUri);
    }

    private void initializeClient(String serverUri) {
        try {
            this.client = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server");
                }

                @Override
                public void onMessage(String message) {
                    // Format: "patientId,timestamp,label,data"
                    String[] parts = message.split(",");
                    if (parts.length == 4) {
                        try {
                            int patientId = Integer.parseInt(parts[0]);
                            long timestamp = Long.parseLong(parts[1]);
                            String label = parts[2];
                            String data = parts[3];

                            // Handle different data types
                            if (label.equals("Alert")) {
                                dataStorage.addPatientData(patientId,
                                        Double.parseDouble(data),
                                        label,
                                        timestamp);
                            } else {
                                // For numerical measurements
                                dataStorage.addPatientData(patientId,
                                        Double.parseDouble(data),
                                        label,
                                        timestamp);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing message: " + message);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from server: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                }
            };
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid WebSocket URI", e);
        }
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.client.connect();
    }

    @Override
    public void stopReading() {
        this.client.close();
    }
}