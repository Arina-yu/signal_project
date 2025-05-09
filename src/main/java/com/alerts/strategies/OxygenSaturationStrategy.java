package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class OxygenSaturationStrategy implements AlertStrategy {
    private static final long TIME_WINDOW_MS = 600000; // 10 minutes
    private static final double CRITICAL_SATURATION = 92; // %
    private static final double RAPID_DROP_THRESHOLD = 5; // % drop within window
    private static final double DROP_RATE_THRESHOLD = 0.5; // % per minute

    @Override
    public Alert checkAlert(Patient patient) {
        List<PatientRecord> records = getRecentRecords(patient, "Saturation", TIME_WINDOW_MS);
        if (records.isEmpty()) return null;

        PatientRecord latestRecord = records.get(records.size()-1);
        double latestValue = latestRecord.getMeasurementValue();
        long latestTime = latestRecord.getTimestamp();

        // Check critical low saturation
        if (latestValue < CRITICAL_SATURATION) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Low Oxygen Saturation: " + latestValue + "%",
                    latestTime
            );
        }

        // Check rapid drop (absolute and rate-based)
        if (records.size() >= 2) {
            PatientRecord oldestRecord = records.get(0);
            double oldestValue = oldestRecord.getMeasurementValue();
            long timeDiffMinutes = (latestTime - oldestRecord.getTimestamp()) / 60000;

            // Absolute drop check
            if (oldestValue - latestValue >= RAPID_DROP_THRESHOLD) {
                return new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Rapid Oxygen Drop: " + (oldestValue - latestValue) + "% over " +
                                timeDiffMinutes + " minutes",
                        latestTime
                );
            }

            // Rate-based drop check (more sensitive)
            if (timeDiffMinutes > 0) {
                double dropRate = (oldestValue - latestValue) / timeDiffMinutes;
                if (dropRate >= DROP_RATE_THRESHOLD) {
                    return new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Fast Oxygen Desaturation: " + String.format("%.1f", dropRate) + "%/minute",
                            latestTime
                    );
                }
            }
        }

        return null;
    }

    private List<PatientRecord> getRecentRecords(Patient patient, String recordType, long timeWindowMs) {
        long startTime = System.currentTimeMillis() - timeWindowMs;
        return patient.getRecords(startTime, Long.MAX_VALUE).stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .sorted(Comparator.comparing(PatientRecord::getTimestamp))
                .collect(Collectors.toList());
    }
}