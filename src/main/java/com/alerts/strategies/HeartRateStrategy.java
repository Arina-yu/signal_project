package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class HeartRateStrategy implements AlertStrategy {
    private static final long TIME_WINDOW_MS = 300000; // 5 minutes
    private static final double MIN_HR = 50; // bpm (брадикардия)
    private static final double MAX_HR = 100; // bpm (тахикардия)
    private static final int IRREGULAR_WINDOW = 5; // количество измерений для проверки нерегулярности

    @Override
    public Alert checkAlert(Patient patient) {
        List<PatientRecord> records = getRecentRecords(patient, "HeartRate", TIME_WINDOW_MS);
        if (records.isEmpty()) return null;

        PatientRecord lastRecord = records.get(records.size() - 1);
        double lastHR = lastRecord.getMeasurementValue();
        long timestamp = lastRecord.getTimestamp();

        // Проверка критических значений
        if (lastHR < MIN_HR) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Bradycardia Alert: " + lastHR + " bpm (< " + MIN_HR + " bpm)",
                    timestamp
            );
        }
        else if (lastHR > MAX_HR) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Tachycardia Alert: " + lastHR + " bpm (> " + MAX_HR + " bpm)",
                    timestamp
            );
        }

        // Проверка на нерегулярный ритм (если достаточно данных)
        if (records.size() >= IRREGULAR_WINDOW && checkIrregularRhythm(records)) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Irregular Heart Rate Detected",
                    timestamp
            );
        }

        return null;
    }

    private boolean checkIrregularRhythm(List<PatientRecord> records) {
        // Вычисляем среднее отклонение между последовательными измерениями
        double totalDiff = 0;
        int count = 0;

        for (int i = 1; i < records.size(); i++) {
            double diff = Math.abs(records.get(i).getMeasurementValue() -
                    records.get(i-1).getMeasurementValue());
            totalDiff += diff;
            count++;
        }

        double avgVariation = totalDiff / count;

        // Если среднее отклонение больше 10% от среднего пульса
        double avgHR = records.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .average()
                .orElse(0);

        return avgVariation > (avgHR * 0.1);
    }

    private List<PatientRecord> getRecentRecords(Patient patient, String recordType, long timeWindowMs) {
        long startTime = System.currentTimeMillis() - timeWindowMs;
        return patient.getRecords(startTime, Long.MAX_VALUE).stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .sorted(Comparator.comparing(PatientRecord::getTimestamp))
                .collect(Collectors.toList());
    }
}