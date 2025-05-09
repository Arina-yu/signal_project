package com.cardio_generator;

import com.cardio_generator.generators.*;
import com.cardio_generator.outputs.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class HealthDataSimulator {

    private static HealthDataSimulator instance;

    private int patientCount = 50;
    private ScheduledExecutorService scheduler;
    private OutputStrategy outputStrategy = new ConsoleOutputStrategy();
    private final Random random = new Random();

    // 🔐 Private constructor for Singleton
    private HealthDataSimulator() {}

    // 🌍 Global access method
    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    // ✅ Public method to start the simulation
    public void start(String[] args) throws IOException {
        parseArguments(args);
        this.scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds);

        scheduleTasksForPatients(patientIds);
    }

    private void parseArguments(String[] args) throws IOException {
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
                            System.err.println("Invalid number of patients. Using default: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        configureOutput(outputArg);
                    }
                    break;
                default:
                    System.err.println("Unknown option: " + args[i]);
                    printHelp();
                    System.exit(1);
            }
        }
    }

    private void configureOutput(String outputArg) throws IOException {
        if (outputArg.equals("console")) {
            outputStrategy = new ConsoleOutputStrategy();
        } else if (outputArg.startsWith("file:")) {
            String baseDir = outputArg.substring(5);
            Path path = Paths.get(baseDir);
            if (!Files.exists(path)) Files.createDirectories(path);
            outputStrategy = new FileOutputStrategy(baseDir);
        } else if (outputArg.startsWith("websocket:")) {
            int port = Integer.parseInt(outputArg.substring(10));
            outputStrategy = new WebSocketOutputStrategy(port);
            System.out.println("WebSocket output on port: " + port);
        } else if (outputArg.startsWith("tcp:")) {
            int port = Integer.parseInt(outputArg.substring(4));
            outputStrategy = new TcpOutputStrategy(port);
            System.out.println("TCP output on port: " + port);
        } else {
            System.err.println("Unknown output type. Using default (console).");
        }
    }

    private void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("  -h                         Show help");
        System.out.println("  --patient-count <count>   Number of patients (default 50)");
        System.out.println("  --output <type>           Output type: console, file:<dir>, websocket:<port>, tcp:<port>");
    }

    private List<Integer> initializePatientIds(int count) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) ids.add(i);
        return ids;
    }

    private void scheduleTasksForPatients(List<Integer> ids) {
        ECGDataGenerator ecg = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator sat = new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bp = new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bl = new BloodLevelsDataGenerator(patientCount);
        AlertGenerator ag = new AlertGenerator(patientCount);

        for (int id : ids) {
            scheduleTask(() -> ecg.generate(id, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> sat.generate(id, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bp.generate(id, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bl.generate(id, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> ag.generate(id, outputStrategy), 20, TimeUnit.SECONDS);
        }
    }

    private void scheduleTask(Runnable task, long period, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, unit);
    }

    //  Static main for running the app
    public static void main(String[] args) throws IOException {
        HealthDataSimulator.getInstance().start(args);
    }
}
