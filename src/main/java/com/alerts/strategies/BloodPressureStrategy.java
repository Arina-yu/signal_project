package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class BloodPressureStrategy implements AlertStrategy {
    private static final int TREND_WINDOW = 3;
    private static final double TREND_THRESHOLD = 10; // mmHg
    private static final long TIME_WINDOW_MS = 600000; // 10 minutes

    // Critical threshold constants
    private static final double CRITICAL_SYSTOLIC_HIGH = 180;
    private static final double CRITICAL_DIASTOLIC_HIGH = 120;
    private static final double CRITICAL_SYSTOLIC_LOW = 90;
    private static final double CRITICAL_DIASTOLIC_LOW = 60;

    @Override
    public Alert checkAlert(Patient patient) {
        List<PatientRecord> systolicRecords = getRecentRecords(patient, "SystolicPressure", TIME_WINDOW_MS);
        List<PatientRecord> diastolicRecords = getRecentRecords(patient, "DiastolicPressure", TIME_WINDOW_MS);

        // Check critical thresholds first
        Alert thresholdAlert = checkCriticalThresholds(systolicRecords, diastolicRecords, patient);
        if (thresholdAlert != null) {
            return thresholdAlert;
        }

        // Check trends if no critical threshold alert
        Alert trendAlert = checkTrendAlerts(systolicRecords, diastolicRecords, patient);
        if (trendAlert != null) {
            return trendAlert;
        }

        return null;
    }

    public Alert checkCriticalThresholds(List<PatientRecord> systolic, List<PatientRecord> diastolic, Patient patient) {
        if (systolic.isEmpty() || diastolic.isEmpty()) {
            return null;
        }

        double lastSystolic = systolic.get(systolic.size()-1).getMeasurementValue();
        double lastDiastolic = diastolic.get(diastolic.size()-1).getMeasurementValue();
        long timestamp = System.currentTimeMillis();

        // Check for high blood pressure crisis
        if (lastSystolic >= CRITICAL_SYSTOLIC_HIGH || lastDiastolic >= CRITICAL_DIASTOLIC_HIGH) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Blood Pressure: " + lastSystolic + "/" + lastDiastolic + " mmHg",
                    timestamp
            );
        }
        // Check for dangerously low blood pressure
        else if (lastSystolic <= CRITICAL_SYSTOLIC_LOW || lastDiastolic <= CRITICAL_DIASTOLIC_LOW) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Low Blood Pressure: " + lastSystolic + "/" + lastDiastolic + " mmHg",
                    timestamp
            );
        }

        return null;
    }

    private Alert checkTrendAlerts(List<PatientRecord> systolic, List<PatientRecord> diastolic, Patient patient) {
        boolean increasingSystolic = checkIncreasingTrend(systolic);
        boolean decreasingSystolic = checkDecreasingTrend(systolic);
        boolean increasingDiastolic = checkIncreasingTrend(diastolic);
        boolean decreasingDiastolic = checkDecreasingTrend(diastolic);

        if (increasingSystolic || increasingDiastolic) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Increasing Blood Pressure Trend Detected",
                    System.currentTimeMillis()
            );
        } else if (decreasingSystolic || decreasingDiastolic) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Decreasing Blood Pressure Trend Detected",
                    System.currentTimeMillis()
            );
        }

        return null;
    }

    public boolean checkIncreasingTrend(List<PatientRecord> records) {
        if (records.size() < TREND_WINDOW) return false;

        for (int i = 0; i < TREND_WINDOW - 1; i++) {
            double diff = records.get(i+1).getMeasurementValue() - records.get(i).getMeasurementValue();
            if (diff < TREND_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    public boolean checkDecreasingTrend(List<PatientRecord> records) {
        if (records.size() < TREND_WINDOW) return false;

        for (int i = 0; i < TREND_WINDOW - 1; i++) {
            double diff = records.get(i+1).getMeasurementValue() - records.get(i).getMeasurementValue();
            if (diff > -TREND_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    public List<PatientRecord> getRecentRecords(Patient patient, String recordType, long timeWindowMs) {
        long startTime = System.currentTimeMillis() - timeWindowMs;
        return patient.getRecords(startTime, Long.MAX_VALUE).stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .sorted(Comparator.comparing(PatientRecord::getTimestamp))
                .collect(Collectors.toList());
    }
}