package com.data_management;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Map;

public class DataReaderClass implements DataReader {

    private final String outputDirectory;

    public DataReaderClass(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        if (dataStorage == null) {
            throw new IllegalArgumentException("DataStorage cannot be null");
        }

        Path dirPath = Paths.get(outputDirectory);
        if (!Files.isDirectory(dirPath)) {
            throw new IOException("Directory not found: " + outputDirectory);
        }

        File directory = new File(outputDirectory);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        List<PatientRecord> records = parseFile(file);
                        storeRecords(dataStorage, records);
                    } catch (Exception e) {
                        // Логируем ошибку, но не бросаем IOException, чтобы не прерывать обработку других файлов
                        System.err.println("⚠️ Error processing file " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void stopReading() {

    }

    public List<PatientRecord> parseFile(File file) throws IOException {
        List<PatientRecord> records = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JSONTokener token = new JSONTokener(reader);
            Object json = token.nextValue();

            if (json instanceof JSONObject) {
                PatientRecord record = parseRecord((JSONObject) json);
                if (record != null) {
                    records.add(record);
                }
            } else if (json instanceof JSONArray) {
                JSONArray array = (JSONArray) json;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        PatientRecord record = parseRecord((JSONObject) item);
                        if (record != null) {
                            records.add(record);
                        }
                    }
                }
            } else {
                System.err.println("Unsupported JSON structure in file: " + file.getName());
            }
        } catch (Exception e) {
            throw new IOException("Invalid JSON format in file: " + file.getName(), e);
        }

        return records;
    }

    public PatientRecord parseRecord(JSONObject jsonRecord) {
        try {
            if (!jsonRecord.has("patientId") ||
                    !jsonRecord.has("recordType") ||
                    !jsonRecord.has("measurementValue") ||
                    !jsonRecord.has("timestamp")) {
                System.err.println("Error parsing record: missing required fields.");
                return null;
            }

            int patientId = jsonRecord.getInt("patientId");
            long timestamp = jsonRecord.getLong("timestamp");
            String recordType = jsonRecord.getString("recordType");
            double measurementValue = jsonRecord.getDouble("measurementValue");

            return new PatientRecord(patientId, measurementValue, recordType, timestamp);
        } catch (Exception e) {
            System.err.println("Error parsing record: " + e.getMessage());
            return null;
        }
    }

    public void storeRecords(DataStorage dataStorage, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            dataStorage.addPatientData(
                    record.getPatientId(),
                    record.getMeasurementValue(),
                    record.getRecordType(),
                    record.getTimestamp()
            );
        }
    }



}
