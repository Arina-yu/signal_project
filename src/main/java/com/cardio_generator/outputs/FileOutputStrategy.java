
package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the {@link OutputStrategy} interface to handle output of patient data for text files.
 * This class writes the patient data (including ID, timestamp, label, and data) to a specified directory.
 * If the directory doesn't exist, it creates it.
 */

public class FileOutputStrategy implements OutputStrategy {

    //Base directory where the output files are stored.
    private String baseDirectory;

    // Map that associates each label with a specific file path.
    public final ConcurrentHashMap<String, String> file_map = new ConcurrentHashMap<>();

    /**
     * Constructs a {@link FileOutputStrategy} instance with the specified base directory.
     *
     * @param baseDirectory  base directory where output files are stored
     */

    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * @output outputs the patient data to a file, creating the necessary directory and file if they don't exist.
     * Method adds data like this:
     * "Patient ID: {patientId}, Timestamp: {timestamp}, Label: {label}, Data: {data}"
     *
     * If an error occurs during the creation of the directory or writing to the file, it is logged to the standard error.
     *
     * @param patientId  id of the patient
     * @param timestamp  timestamp of the data
     * @param label  label associated with the data
     * @param data  data to be written to the file
     */

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }

        // Set the file path for the label
        String filePath = file_map.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}