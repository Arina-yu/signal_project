package com.alerts;

import java.util.*;
import java.util.stream.Collectors;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class AlertGenerator {
    private static final int TREND_WINDOW = 3;
    private static final double TREND_THRESHOLD = 10;
    private static final int ECG_ANALYSIS_WINDOW = 30;
    private static final double ECG_DEVIATION_THRESHOLD = 3;

    public void evaluateData(Patient patient) {
        checkBloodPressureAlerts(patient);
        checkSaturationAlerts(patient);
        checkECGAlerts(patient);
        checkManualAlerts(patient);
    }

    private void checkBloodPressureAlerts(Patient patient) {
        List<PatientRecord> systolic = getRecentRecords(patient, "SystolicPressure");
        List<PatientRecord> diastolic = getRecentRecords(patient, "DiastolicPressure");
        List<PatientRecord> saturation = getRecentRecords(patient, "Saturation");

        checkTrend(systolic, "Systolic", patient);
        checkTrend(diastolic, "Diastolic", patient);
        checkCriticalThresholds(systolic, diastolic, patient);
        checkHypotensiveHypoxemia(systolic, saturation, patient);
    }
    private void checkCriticalThresholds(List<PatientRecord> systolic, List<PatientRecord> diastolic, Patient patient) {
        if (systolic.isEmpty() || diastolic.isEmpty()) return;

        PatientRecord latestSys = systolic.get(systolic.size() - 1);
        PatientRecord latestDia = diastolic.get(diastolic.size() - 1);
        long timestamp = Math.max(latestSys.getTimestamp(), latestDia.getTimestamp());
        String patientId = String.valueOf(latestSys.getPatientId());

        if (latestSys.getMeasurementValue() > 180 || latestDia.getMeasurementValue() > 120) {
            triggerAlert(new Alert(patientId, "Hypertensive Crisis Detected", timestamp));
        } else if (latestSys.getMeasurementValue() < 90 || latestDia.getMeasurementValue() < 60) {
            triggerAlert(new Alert(patientId, "Hypotension Detected", timestamp));
        }
    }

    private void checkHypotensiveHypoxemia(List<PatientRecord> systolic, List<PatientRecord> saturation, Patient patient) {
        if (systolic.isEmpty() || saturation.isEmpty()) return;

        // Get most recent readings within 10-minute window
        PatientRecord lastSystolic = systolic.get(systolic.size() - 1);
        PatientRecord lastSaturation = saturation.get(saturation.size() - 1);

        // Check if readings are within 10 minutes of each other
        long timeDifference = Math.abs(lastSystolic.getTimestamp() - lastSaturation.getTimestamp());
        if (timeDifference > 600000) return; // 10 minutes in milliseconds

        if (lastSystolic.getMeasurementValue() < 90 && lastSaturation.getMeasurementValue() < 92) {
            triggerAlert(new Alert(
                    String.valueOf(PatientRecord.getPatientId()),
                    "Hypotensive Hypoxemia Alert: BP=" + lastSystolic.getMeasurementValue() +
                            " mmHg, O2 Sat=" + lastSaturation.getMeasurementValue() + "%",
                    Math.max(lastSystolic.getTimestamp(), lastSaturation.getTimestamp())
            ));
        }
    }
    private void checkRapidDrop(List<PatientRecord> records, Patient patient) {
        if (records.size() < 2) return;

        // Find maximum and minimum values in the window
        double max = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .max()
                .orElse(100); // Default to normal value if empty

        double min = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .min()
                .orElse(100);

        double drop = max - min;
        if (drop >= 5) {
            triggerAlert(new Alert(
                    String.valueOf(PatientRecord.getPatientId()),
                    "Rapid Oxygen Drop Detected: " + String.format("%.1f", drop) + "%",
                    records.get(records.size() - 1).getTimestamp()
            ));
        }
    }

    private void checkSaturationAlerts(Patient patient) {
        List<PatientRecord> records = getRecentRecords(patient, "Saturation");
        checkLowSaturation(records, patient);
        checkRapidDrop(records, patient);
    }
    private void checkLowSaturation(List<PatientRecord> records, Patient patient) {
        if (records.isEmpty()) return;

        PatientRecord latest = records.get(records.size() - 1);
        if (latest.getMeasurementValue() < 92) {
            triggerAlert(new Alert(
                    String.valueOf(PatientRecord.getPatientId()),
                    "Low Blood Oxygen Saturation: " + latest.getMeasurementValue() + "%",
                    latest.getTimestamp()
            ));
        }
    }

    private void checkECGAlerts(Patient patient) {
        List<PatientRecord> records = getRecentRecords(patient, "ECG");
        if (records.size() < ECG_ANALYSIS_WINDOW) return;

        double[] stats = calculateStats(records);
        PatientRecord latest = records.get(records.size()-1);

        if (Math.abs(latest.getMeasurementValue() - stats[0]) > ECG_DEVIATION_THRESHOLD * stats[1]) {
            triggerAlert(new Alert(
                    String.valueOf(latest.getPatientId()),
                    "ECG Abnormality: Value " + latest.getMeasurementValue(),
                    System.currentTimeMillis()
            ));
        }
    }

    private void checkManualAlerts(Patient patient) {
        getRecentRecords(patient, "Alert").stream()
                .filter(r -> r.getMeasurementValue() == 1)
                .findFirst()
                .ifPresent(r -> triggerAlert(new Alert(
                        String.valueOf(PatientRecord.getPatientId()),
                        "Manual Alert Triggered",
                        r.getTimestamp()
                )));
    }

    // Helper methods
    private List<PatientRecord> getRecentRecords(Patient patient, String type) {
        long windowStart = System.currentTimeMillis() - 600000; // 10 minutes
        return patient.getRecords(windowStart, Long.MAX_VALUE).stream()
                .filter(r -> r.getRecordType().equals(type))
                .sorted(Comparator.comparing(PatientRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    private void checkTrend(List<PatientRecord> records, String type, Patient patient) {
        if (records.size() < TREND_WINDOW) return;

        double[] diffs = new double[TREND_WINDOW-1];
        for (int i = 0; i < diffs.length; i++) {
            diffs[i] = records.get(i+1).getMeasurementValue() - records.get(i).getMeasurementValue();
        }

        if (Arrays.stream(diffs).allMatch(d -> d > TREND_THRESHOLD)) {
            triggerTrendAlert(patient, type, "Increasing");
        } else if (Arrays.stream(diffs).allMatch(d -> d < -TREND_THRESHOLD)) {
            triggerTrendAlert(patient, type, "Decreasing");
        }
    }

    private void triggerTrendAlert(Patient patient, String type, String trend) {
        triggerAlert(new Alert(
                String.valueOf(PatientRecord.getPatientId()),
                trend + " " + type + " Pressure Trend",
                System.currentTimeMillis()
        ));
    }

    private double[] calculateStats(List<PatientRecord> records) {
        double mean = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .average().orElse(0);
        double stdDev = Math.sqrt(records.stream()
                .mapToDouble(r -> Math.pow(r.getMeasurementValue() - mean, 2))
                .average().orElse(0));
        return new double[]{mean, stdDev};
    }

    protected void triggerAlert(Alert alert) {
        System.out.printf("ALERT: %s for Patient %s at %tF %<tT%n",
                alert.getCondition(),
                alert.getPatientId(),
                new Date(alert.getTimestamp()));
    }
}