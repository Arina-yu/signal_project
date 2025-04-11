package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

public class FileOutputStrategy implements OutputStrategy {
    // Rule: 5.1 - Variable names are written in lowerCamelCase.
    private String baseDirectory;

    // Rule: 5.1 - Constants are written in UPPER_CASE, but this is not a constant. Therefore, use lowerCamelCase.
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    // Rule: 5.1 - Parameter names use lowerCamelCase.
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    @Override
    public void output(int patientId, long timeStamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // Rule: 5.1 - Variable names are written in lowerCamelCase.
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());
        //filePath should start from lower case letter as it it a variable

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timeStamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}