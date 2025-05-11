package com.data_management;

import com.alerts.AlertGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//Singleton class for managing storage and retrieval of patient data in a healthcare monitoring system.
public class DataStorage {
    private static DataStorage instance;
    private final Map<Integer, Patient> patientMap;

    // Modified constructor to be protected for testing
    public DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    // New method for test injection
    public static synchronized void setInstance(DataStorage testInstance) {
        instance = testInstance;
    }




    public static void main(String[] strings) {
    }

     // Adds or updates patient data in the storage.
    public void addPatientData(int patientId, double measurementValue,
                               String recordType, long timestamp) {
        patientMap.compute(patientId, (id, patient) -> {
            if (patient == null) {
                patient = new Patient(id);
            }
            patient.addRecord(measurementValue, recordType, timestamp);
            return patient;
        });
    }

    //Retrieves patient records for a specific time range
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        return (patient != null) ? patient.getRecords(startTime, endTime) : new ArrayList<>();
    }


     //Returns all patients in storage
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }}