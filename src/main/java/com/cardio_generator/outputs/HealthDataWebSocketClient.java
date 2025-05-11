package com.cardio_generator.outputs;


import com.data_management.DataStorage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
/**
 * A WebSocket client implementation receives&processes real time health data.
 * When a client connects to a WebSocket server, he receives patient data messages, stores them in DataStorage,
 * and in case of errors handles them with reconnection attempts
 *
 * <p>The expected message format is: "patientId,timestamp,label,measurement" where:
 * <ul>
 *   <li>patientId - integer identifier of the patient</li>
 *   <li>timestamp - long value representing measurement time in milliseconds</li>
 *   <li>label - string describing the measurement type (e.g., "HeartRate")</li>
 *   <li>measurement - double value of the actual measurement</li>
 * </ul>
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic reconnection with 5-second delay on connection loss</li>
 *   <li>Thread-safe reconnection mechanism</li>
 *   <li>Data validation and error logging</li>
 *   <li>Integration with DataStorage for persistent data management</li>
 * </ul>
 */
public class HealthDataWebSocketClient extends WebSocketClient {

    private final DataStorage dataStorage;
    private final URI serverUri;
    private static final int reconnectDelay = 5000; // = 5 seconds
    private boolean reconnecting = false;

    /**
     * Constructs a new WebSocket client for health data.
     *
     * @param serverUri the WebSocket server URI (e.g., "ws://localhost:8080/data")
     * @param storage the DataStorage instance where received data will be persisted
     */
    public HealthDataWebSocketClient(URI serverUri, DataStorage storage) {
        super(serverUri);
        this.serverUri = serverUri;
        this.dataStorage = storage;
    }
    /**
     * Called when the WebSocket connection is successfully established.
     * Resets the reconnection flag and logs the connection status.
     *
     * @param handshakedata the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to WebSocket server");
        reconnecting = false;
    }
    /**
     * Processes incoming WebSocket messages containing health data.
     * Parses messages in format "patientId,timestamp,label,measurement" and stores them.
     *
     * <p>Error handling:
     * <ul>
     *   <li>Logs invalid message formats</li>
     *   <li>Catches and logs parsing errors</li>
     *   <li>Prints stack traces for debugging</li>
     * </ul>
     *
     * @param message the raw message received from WebSocket
     */
    @Override
    public void onMessage(String message) {
        // Here we convert and store parsed data(patientIds, timestamp, labels and measurements
        // Each part is parsed into its appropriate data type
        //Then after successful parsing the data is passed to the DataStorage instance
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
    /**
     * Handles connection closure events.
     * Triggers automatic reconnection after the specified delay.
     *
     * @param code the closure code
     * @param reason the closure reason
     * @param remote whether the closure was initiated by the remote host
     */
    //called when the connection is closed
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
        attemptReconnect();
    }
    /**
     * Handles WebSocket communication errors.
     * Logs the error and initiates reconnection.
     *
     * @param ex the exception that occurred
     */
    //called when an error occurs(for example if the message does not have exactly 4 "parts", an error is logged and we skip the message)
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error:");
        ex.printStackTrace();
        attemptReconnect();
    }
    /**
     * Manages reconnection logic with thread-safe protection against duplicate attempts.
     * Creates a new client instance after the delay period.
     *
     * <p>Features:
     * <ul>
     *   <li>Synchronized reconnection flag prevents multiple attempts</li>
     *   <li>Uses TimerTask for delayed execution</li>
     *   <li>Creates new client instances to avoid issues with connection state </li>
     * </ul>
     */
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