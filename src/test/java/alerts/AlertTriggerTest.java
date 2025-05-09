package alerts;

import com.alerts.AlertGenerator;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertTriggerTest {
    public AlertGenerator alertGenerator;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        alertGenerator = new AlertGenerator();
        testPatient = new Patient(1);
    }

    @Test
    void testBloodPressureTrendIncreasingAlert() {
        addRecords(testPatient, "SystolicPressure", 100, 115, 130);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testBloodPressureTrendDecreasingAlert() {
        addRecords(testPatient, "DiastolicPressure", 80, 65, 50);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalSystolicHighAlert() {
        addRecord(testPatient, "SystolicPressure", 185, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalDiastolicLowAlert() {
        addRecord(testPatient, "DiastolicPressure", 55, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testLowSaturationAlert() {
        addRecord(testPatient, "Saturation", 91, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidSaturationDropAlert() {
        long now = System.currentTimeMillis();
        addRecord(testPatient, "Saturation", 97, now - 5000);
        addRecord(testPatient, "Saturation", 91, now);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        long now = System.currentTimeMillis();
        addRecord(testPatient, "SystolicPressure", 85, now);
        addRecord(testPatient, "Saturation", 90, now + 1000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testECGAbnormalityAlert() {
        for (int i = 0; i < 30; i++) {
            addRecord(testPatient, "ECG", 1.0, System.currentTimeMillis() - (30 - i) * 1000);
        }
        addRecord(testPatient, "ECG", 5.0, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testManualAlertTriggered() {
        addRecord(testPatient, "Alert", 1, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testNoAlertWhenDataNormal() {
        addRecord(testPatient, "SystolicPressure", 120, System.currentTimeMillis());
        addRecord(testPatient, "DiastolicPressure", 80, System.currentTimeMillis());
        addRecord(testPatient, "Saturation", 98, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testEdgeCaseMinimumRecordsForTrend() {
        addRecords(testPatient, "SystolicPressure", 100, 115);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testBoundaryValuesForThresholds() {
        addRecord(testPatient, "SystolicPressure", 90, System.currentTimeMillis());
        addRecord(testPatient, "DiastolicPressure", 60, System.currentTimeMillis());
        alertGenerator.evaluateData(testPatient);
    }

    private void addRecord(Patient patient, String recordType, double value, long timestamp) {
        patient.addRecord(value, recordType, timestamp);
    }

    private void addRecords(Patient patient, String recordType, double... values) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < values.length; i++) {
            patient.addRecord(values[i], recordType, now - (values.length - i - 1) * 1000);
        }
    }
}
