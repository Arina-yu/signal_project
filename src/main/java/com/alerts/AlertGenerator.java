package com.alerts;

import java.util.*;
import java.util.stream.Collectors;

import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Generates medical alerts based on patient data analysis.
 * Uses Factory Method pattern for blood pressure, oxygen, and ECG alerts.
 */
public class AlertGenerator {
    // Constants for analysis
    private static final int TREND_WINDOW = 3;
    private static final double TREND_THRESHOLD = 10;
    private static final int ECG_ANALYSIS_WINDOW = 30;
    private static final double ECG_DEVIATION_THRESHOLD = 3;
    private static final long TIME_WINDOW_MS = 600000; // 10 minutes

    // Factories for medical alerts
    private final AlertFactory bloodPressureFactory = new BloodPressureAlertFactory();
    private final AlertFactory bloodOxygenFactory = new BloodOxygenAlertFactory();
    private final AlertFactory ecgFactory = new ECGAlertFactory();

    public void evaluateData(Patient patient) {
        checkBloodPressureAlerts(patient);
        checkSaturationAlerts(patient);
        checkECGAlerts(patient);
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

    private void checkCriticalThresholds(List<PatientRecord> systolic,
                                         List<PatientRecord> diastolic,
                                         Patient patient) {
        if (systolic.isEmpty() || diastolic.isEmpty()) return;

        PatientRecord lastSystolic = systolic.get(systolic.size() - 1);
        PatientRecord lastDiastolic = diastolic.get(diastolic.size() - 1);
        long timestamp = Math.max(lastSystolic.getTimestamp(), lastDiastolic.getTimestamp());

        if (lastSystolic.getMeasurementValue() > 180 || lastDiastolic.getMeasurementValue() > 120) {
            triggerAlert(bloodPressureFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Critical BP: " + lastSystolic.getMeasurementValue() +
                            "/" + lastDiastolic.getMeasurementValue() + " mmHg",
                    timestamp
            ));
        } else if (lastSystolic.getMeasurementValue() < 90 || lastDiastolic.getMeasurementValue() < 60) {
            triggerAlert(bloodPressureFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Low BP: " + lastSystolic.getMeasurementValue() +
                            "/" + lastDiastolic.getMeasurementValue() + " mmHg",
                    timestamp
            ));
        }
    }

    private void checkHypotensiveHypoxemia(List<PatientRecord> systolic,
                                           List<PatientRecord> saturation,
                                           Patient patient) {
        if (systolic.isEmpty() || saturation.isEmpty()) return;

        PatientRecord lastSystolic = systolic.get(systolic.size() - 1);
        PatientRecord lastSaturation = saturation.get(saturation.size() - 1);

        if (Math.abs(lastSystolic.getTimestamp() - lastSaturation.getTimestamp()) > 600000) {
            return;
        }

        if (lastSystolic.getMeasurementValue() < 90 && lastSaturation.getMeasurementValue() < 92) {
            triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia: BP=" + lastSystolic.getMeasurementValue() +
                            " mmHg, O2=" + lastSaturation.getMeasurementValue() + "%",
                    Math.max(lastSystolic.getTimestamp(), lastSaturation.getTimestamp())
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
            triggerAlert(bloodOxygenFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Low Oxygen: " + latest.getMeasurementValue() + "%",
                    latest.getTimestamp()
            ));
        }
    }

    private void checkRapidDrop(List<PatientRecord> records, Patient patient) {
        if (records.size() < 2) return;

        double max = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .max()
                .orElse(100);
        double min = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .min()
                .orElse(100);

        if (max - min >= 5) {
            triggerAlert(bloodOxygenFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Rapid O2 Drop: " + String.format("%.1f", max - min) + "%",
                    records.get(records.size() - 1).getTimestamp()
            ));
        }
    }

    private void checkECGAlerts(Patient patient) {
        List<PatientRecord> records = getRecentRecords(patient, "ECG");
        if (records.size() < ECG_ANALYSIS_WINDOW) return;

        double[] stats = calculateStats(records);
        PatientRecord latest = records.get(records.size() - 1);

        if (Math.abs(latest.getMeasurementValue() - stats[0]) > ECG_DEVIATION_THRESHOLD * stats[1]) {
            triggerAlert(ecgFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "ECG Abnormality: " + latest.getMeasurementValue() +
                            " (σ=" + String.format("%.1f", stats[1]) + ")",
                    latest.getTimestamp()
            ));
        }
    }

    // Helper methods
    private List<PatientRecord> getRecentRecords(Patient patient, String type) {
        long windowStart = System.currentTimeMillis() - TIME_WINDOW_MS;
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
            triggerAlert(bloodPressureFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Increasing " + type + " Trend",
                    System.currentTimeMillis()
            ));
        } else if (Arrays.stream(diffs).allMatch(d -> d < -TREND_THRESHOLD)) {
            triggerAlert(bloodPressureFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Decreasing " + type + " Trend",
                    System.currentTimeMillis()
            ));
        }
    }

    private double[] calculateStats(List<PatientRecord> records) {
        double mean = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(records.stream()
                .mapToDouble(r -> Math.pow(r.getMeasurementValue() - mean, 2))
                .average()
                .orElse(0));
        return new double[]{mean, stdDev};
    }

    protected void triggerAlert(Alert alert) {
        System.out.printf("ALERT: %s for Patient %s at %tF %<tT%n",
                alert.getCondition(),
                alert.getPatientId(),
                new Date(alert.getTimestamp()));
    }
}