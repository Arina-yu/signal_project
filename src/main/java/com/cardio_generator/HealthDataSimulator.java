package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;

import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
/**
 * Simulates health data for multiple patients and outputs the generated data using various output strategies.
 * <p>
 * This class serves as the main entry point for running the health data simulation. It is responsible for
 * processing command-line arguments, initializing the necessary components (e.g., patient data generators, output strategy),
 * and scheduling periodic tasks for generating health data for each patient.
 * </p>
 * <p>
 * The health data includes ECG, blood pressure, blood saturation, blood levels, and alerts. The data can be output to
 * the console, files, WebSocket server, or TCP sockets depending on the user's configuration.
 * </p>
 *
 * @author Oryna Yukhymenko
 * @author Elena Gostiukhina
 */

public class HealthDataSimulator {

    private static int patientCount = 50; // Default number of patients
    private static ScheduledExecutorService scheduler;
    private static OutputStrategy outputStrategy = new ConsoleOutputStrategy(); // Default output strategy
    private static final Random random = new Random();
    /**
     * The main entry point of the HealthDataSimulator application.
     * <p>
     * This method processes command-line arguments, initializes the patient data, and schedules the generation
     * of health data at fixed intervals for each patient. Based on the configuration, the data is output using
     * the selected output strategy (console, file, WebSocket, or TCP).
     * </p>
     *
     * @param args The command-line arguments used to configure the simulator.
     *             <ul>
     *             <li>-h: Show help</li>
     *             <li>--patient-count <count>: Number of patients to simulate</li>
     *             <li>--output <type>: Output method (console, file, websocket:<port>, tcp:<port>)</li>
     *             </ul>
     * @throws IOException If an error occurs while reading or writing files.
     */

    public static void main(String[] args) throws IOException {

        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds); // Randomize the order of patient IDs

        scheduleTasksForPatients(patientIds);
    }
    /**
     * Parses the command-line arguments and configures the simulator based on the options provided.
     * <p>
     * This method identifies various options for configuring the number of patients and the output method (console,
     * file, WebSocket, or TCP socket). It sets up the simulator with the appropriate settings.
     * </p>
     *
     * @param args The command-line arguments passed to the application.
     * @throws IOException If there is an issue creating directories for file output.
     */

    private static void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err
                                    .println("Error: Invalid number of patients. Using default value: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                // Initialize your WebSocket output strategy here
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println(
                                        "Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                // Initialize your TCP socket output strategy here
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }
    /**
     * Prints the help message to the console, explaining the available command-line options.
     * <p>
     * This method provides an overview of how to run the application and use the available options.
     * </p>
     */
    private static void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println(
                "  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
        System.out.println(
                "  This command simulates data for 100 patients and sends the output to WebSocket clients connected to port 8080.");
    }
    /**
     * Initializes a list of patient IDs, starting from 1 up to the specified patient count.
     * <p>
     * This method generates a list of patient IDs, ensuring that each patient has a unique identifier.
     * </p>
     *
     * @param patientCount The number of patients to simulate. This defines the total number of IDs to generate.
     * @return A list of patient IDs from 1 to patientCount.
     */
    private static List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }
    /**
     * Schedules tasks to generate health data for each patient at specific intervals.
     * <p>
     * This includes tasks for ECG, blood saturation, blood pressure, blood levels, and alerts.
     * Each patient is assigned data generation tasks at fixed intervals, which are then executed by the scheduler.
     * </p>
     *
     * @param patientIds A list of patient IDs for which data generation tasks will be scheduled.
     */
    private static void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(patientCount);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, outputStrategy), 20, TimeUnit.SECONDS);
        }
    }
    /**
     * Schedules a task to be executed at a fixed rate with a delay before the first execution.
     * <p>
     * This method ensures that the provided task will run at regular intervals according to the specified period.
     * </p>
     *
     * @param task The task to be scheduled for execution.
     * @param period The period between consecutive executions of the task.
     * @param timeUnit The time unit for the period (e.g., SECONDS, MINUTES).
     */
    private static void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }
}
