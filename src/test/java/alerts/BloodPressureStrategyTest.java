package alerts;

import com.alerts.Alert;
import com.alerts.strategies.BloodPressureStrategy;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodPressureStrategyTest {
    private BloodPressureStrategy strategy;
    private Patient patient;
    private long currentTime;

    @BeforeEach
    void setUp() {
        strategy = new BloodPressureStrategy();
        patient = new Patient(123);
        currentTime = System.currentTimeMillis();
    }

    @Test
    void testCheckCriticalThresholds_HighPressure() {
        addRecord("SystolicPressure", 185, currentTime - 5000);
        addRecord("DiastolicPressure", 125, currentTime - 5000);

        List<PatientRecord> systolic = strategy.getRecentRecords(patient, "SystolicPressure", 600000);
        List<PatientRecord> diastolic = strategy.getRecentRecords(patient, "DiastolicPressure", 600000);

        Alert alert = strategy.checkCriticalThresholds(systolic, diastolic, patient);
        assertNotNull(alert);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Critical Blood Pressure: 185.0/125.0"));
    }

    @Test
    void testCheckCriticalThresholds_LowPressure() {
        addRecord("SystolicPressure", 85, currentTime - 5000);
        addRecord("DiastolicPressure", 55, currentTime - 5000);

        List<PatientRecord> systolic = strategy.getRecentRecords(patient, "SystolicPressure", 600000);
        List<PatientRecord> diastolic = strategy.getRecentRecords(patient, "DiastolicPressure", 600000);

        Alert alert = strategy.checkCriticalThresholds(systolic, diastolic, patient);
        assertNotNull(alert);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Low Blood Pressure: 85.0/55.0"));
    }

    @Test
    void testCheckCriticalThresholds_NormalPressure() {
        addRecord("SystolicPressure", 120, currentTime - 5000);
        addRecord("DiastolicPressure", 80, currentTime - 5000);

        List<PatientRecord> systolic = strategy.getRecentRecords(patient, "SystolicPressure", 600000);
        List<PatientRecord> diastolic = strategy.getRecentRecords(patient, "DiastolicPressure", 600000);

        Alert alert = strategy.checkCriticalThresholds(systolic, diastolic, patient);
        assertNull(alert);
    }

    @Test
    void testCheckIncreasingTrend_Systolic() {
        List<PatientRecord> records = new ArrayList<>();
        records.add(new PatientRecord(123, 120, "SystolicPressure", currentTime - 20000));
        records.add(new PatientRecord(123, 135, "SystolicPressure", currentTime - 10000));
        records.add(new PatientRecord(123, 150, "SystolicPressure", currentTime));

        assertTrue(strategy.checkIncreasingTrend(records));
    }

    @Test
    void testCheckDecreasingTrend_Diastolic() {
        List<PatientRecord> records = new ArrayList<>();
        records.add(new PatientRecord(123, 90, "DiastolicPressure", currentTime - 20000));
        records.add(new PatientRecord(123, 75, "DiastolicPressure", currentTime - 10000));
        records.add(new PatientRecord(123, 60, "DiastolicPressure", currentTime));

        assertTrue(strategy.checkDecreasingTrend(records));
    }

    @Test
    void testCheckTrendAlerts_IncreasingTrend() {
        addRecord("SystolicPressure", 120, currentTime - 20000);
        addRecord("SystolicPressure", 135, currentTime - 10000);
        addRecord("SystolicPressure", 150, currentTime);

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Increasing Blood Pressure Trend Detected"));
    }

    @Test
    void testCheckTrendAlerts_DecreasingTrend() {
        addRecord("DiastolicPressure", 90, currentTime - 20000);
        addRecord("DiastolicPressure", 75, currentTime - 10000);
        addRecord("DiastolicPressure", 60, currentTime);

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Decreasing Blood Pressure Trend Detected"));
    }

    @Test
    void testGetRecentRecords_Filtering() {
        addRecord("SystolicPressure", 120, currentTime - 300000);
        addRecord("DiastolicPressure", 80, currentTime - 300000);
        addRecord("SystolicPressure", 125, currentTime - 700000); // should be excluded
        addRecord("OtherType", 100, currentTime - 10000); // should be excluded

        List<PatientRecord> systolic = strategy.getRecentRecords(patient, "SystolicPressure", 600000);
        assertEquals(1, systolic.size());
        assertEquals(120, systolic.get(0).getMeasurementValue());
    }



    private void addRecord(String recordType, double value, long timestamp) {
        patient.addRecord(value, recordType, timestamp);
    }
}
