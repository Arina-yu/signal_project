package com.data_management;

import com.alerts.AlertGenerator;

import java.util.*;

/**
 * Singleton class for managing storage and retrieval of patient data
 * within a healthcare monitoring system.
 */
public class DataStorage {

    private static DataStorage instance;

    // Хранилище пациентов
    private final Map<Integer, Patient> patientMap;

    public DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Returns the singleton instance of DataStorage.
     * Ensures that only one instance is created.
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    public static void main(String[] strings) {
    }

    /**
     * Adds or updates patient data in the storage.
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves patient records for a specific time range.
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        return (patient != null) ? patient.getRecords(startTime, endTime) : new ArrayList<>();
    }

    /**
     * Returns all patients in storage.
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }}