package com.cardio_generator.outputs;


import com.data_management.DataStorage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class HealthDataWebSocketClient extends WebSocketClient {

    private final DataStorage dataStorage;
    private final URI serverUri;
    private static final int reconnectDelay = 5000; // = 5 seconds
    private boolean reconnecting = false;


    public HealthDataWebSocketClient(URI serverUri, DataStorage storage) {
        super(serverUri);
        this.serverUri = serverUri;
        this.dataStorage = storage;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to WebSocket server");
        reconnecting = false;
    }

    @Override
    public void onMessage(String message) {
        try {
            String[] parts = message.split(",", 4);
            if (parts.length != 4) {
                System.err.println("Invalid message format: " + message);
                return;
            }

            int patientId = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            String label = parts[2];
            double measurement = Double.parseDouble(parts[3]);

            // Store the data in the DataStorage instance
            dataStorage.addPatientData(patientId, measurement, label, timestamp);
            System.out.println("Stored: " + message);
        } catch (Exception e) {
            System.err.println("Failed to parse or store message: " + message);
            e.printStackTrace();
        }
    }

    //called when the connection is closed
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
        attemptReconnect();
    }

    //called when an error occurs
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error:");
        ex.printStackTrace();
        attemptReconnect();
    }

    //called when the connection is opened
    private void attemptReconnect() {
        if (reconnecting) return;

        reconnecting = true;
        System.out.println("Attempting to reconnect in " + (reconnectDelay / 1000) + " seconds...");

        // Schedules a reconnection attempt after the specified delay time
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    HealthDataWebSocketClient newClient = new HealthDataWebSocketClient(serverUri, dataStorage);
                    newClient.connect();
                } catch (Exception e) {
                    System.err.println("Failed to reconnect:");
                    e.printStackTrace();
                    reconnecting = false;
                }
            }
        }, reconnectDelay);
    }
}