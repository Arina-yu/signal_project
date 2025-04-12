package com.cardio_generator.outputs;
/**
 * Defines the contract for output strategies used in the health data simulator.
 * <p>
 * This interface is implemented by classes responsible for outputting health data in various formats and to different destinations,
 * such as console, files, WebSockets, or TCP sockets. Implementations of this interface must define how to handle and output
 * patient health data (e.g., ECG, blood pressure, blood saturation).
 * </p>
 * <p>
 * The method {@link #output(int, long, String, String)} is used to send the generated health data for a specific patient,
 * along with a timestamp and label.
 * </p>
 *
 * @author Oryna Yukhymenko
 * @author Elena Gostiukhina
 */
public interface OutputStrategy {
    /**
     * Outputs the health data for a certain patient
     * <p>
     * This method is responsible for outputting the health data in the desired format or destination.
     * It takes in the patient ID, timestamp, label, and the actual data to be output. The method will handle the
     * appropriate way to format and transmit the data based on the output strategy's implementation (e.g., print to console,
     * write to file, send over WebSocket, etc.).
     * </p>
     *
     * @param patientId The unique identifier for the patient whose data is being output.
     *                  This is used to associate the data with the correct patient.
     * @param timestamp The timestamp when the data was generated. This is used to time-stamp the data for accurate records.
     * @param label The label or type of the data (e.g., "ECG", "Blood Pressure"). This helps identify the kind of data being sent.
     * @param data The actual health data being generated (e.g., "120/80" for blood pressure). This is the value being transmitted.
     */
    void output(int patientId, long timestamp, String label, String data);
}
