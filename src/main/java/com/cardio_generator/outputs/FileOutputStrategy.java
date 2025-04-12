package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Implementation of {@link OutputStrategy} that writes health data to a file.
 * <p>
 * This class is responsible for outputting patient data to text files. Each file is named according to the
 * label passed to the {@link #output(int, long, String, String)} method (e.g., "Alert.txt"). The data for each
 * patient is appended to the corresponding file.
 * </p>
 * <p>
 * The class ensures that the required directory is created, if it doesn't already exist, and then appends the
 * patient data to the appropriate file, ensuring the files are organized by their labels.
 * </p>
 *
 * @author Oryna Yukhymenko
 * @author Elena Gostiukhina
 */
public class FileOutputStrategy implements OutputStrategy {
    // Rule: 5.1 - Variable names are written in lowerCamelCase.
    private String baseDirectory;
    /**
     * A map of label names to file paths. This ensures that files are created for each unique label
     * (e.g., "Alert", "BloodPressure") and data is appended accordingly.
     */
    // Rule: 5.1 - Constants are written in UPPER_CASE, but this is not a constant. Therefore, use lowerCamelCase.
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();
    /**
     * Constructs a {@link FileOutputStrategy} instance with the specified base directory.
     * <p>
     * The base directory is where the output files will be stored. If the directory does not exist,
     * it will be created when the first output is written.
     * </p>
     *
     * @param baseDirectory The base directory where the output files will be stored.
     *                      The directory will be created if it does not already exist.
     */
    // Rule: 5.1 - Parameter names use lowerCamelCase.
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }
    /**
     * Outputs patient data to a file based on the provided label.
     * <p>
     * The method first ensures that the base directory exists, then appends the provided patient data
     * to the appropriate file determined by the label. The file is created if it does not exist.
     * </p>
     *
     * @param patientId The unique identifier for the patient whose data is being output.
     *                  This helps associate the generated data with a specific patient.
     * @param timeStamp The timestamp when the data is generated. This is used to log when the data was recorded.
     * @param label A label that determines the file in which the data will be written. For example, an "Alert" label
     *              will write the data to "Alert.txt".
     * @param data The actual health data to be written to the file. This can be any type of data related to the label.
     * @throws IOException If an error occurs while creating directories, opening files, or writing to the file,
     *                     an {@link IOException} will be thrown.
     */
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