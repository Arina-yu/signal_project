package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
/**
 * Implementation of the {@link OutputStrategy} interface for TCP socket communication.
 * <p>
 * This class establishes a TCP server that listens on a specified port and sends patient health data to a connected client.
 * It uses a {@link ServerSocket} to accept client connections and sends the data to the client through a {@link PrintWriter}.
 * </p>
 * <p>
 * The output data is formatted as a CSV string containing the patient ID, timestamp, label, and the health data.
 * </p>
 *
 * @author Oryna Yukhymenko
 * @author Elena Gostiukhina
 */
public class TcpOutputStrategy implements OutputStrategy {


    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    /**
     * Constructs a {@link TcpOutputStrategy} that listens for client connections on the specified port.
     * <p>
     * The server is started in a separate thread to accept incoming client connections asynchronously.
     * Once a client connects, the output stream is initialized for sending data.
     * </p>
     *
     * @param port The port on which the TCP server will listen for incoming connections.
     * @throws IOException If an I/O error occurs while setting up the server or accepting connections.
     */

    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Outputs the health data for a specific patient to the connected TCP client.
     * <p>
     * The method sends the patient ID, timestamp, label, and the health data in a comma-separated format to the client.
     * If no client is connected yet, the data is not sent.
     * </p>
     *
     * @param patientId The unique identifier for the patient whose data is being output.
     *                  This value is used to identify the patient whose health data is being sent.
     * @param timestamp The timestamp when the data was generated. This timestamp is included in the output to track when the data was recorded.
     * @param label The label or type of the data (e.g., "ECG", "Blood Pressure"). This helps the client understand the type of the data.
     * @param data The actual health data to be transmitted (e.g., "120/80" for blood pressure). This is the value being sent to the client.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
