package alerts;

import com.alerts.Alert;
import com.alerts.strategies.HeartRateStrategy;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeartRateStrategyTest {
    private HeartRateStrategy strategy;
    private Patient patient;
    private long now;

    @BeforeEach
    void setUp() {
        strategy = new HeartRateStrategy();
        patient = new Patient(101);
        now = System.currentTimeMillis();
    }

    @Test
    void testBradycardiaDetected() {
        addRecord(45); // ниже 50

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Bradycardia"));
    }

    @Test
    void testTachycardiaDetected() {
        addRecord(110); // выше 100

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Tachycardia"));
    }

    @Test
    void testNormalHeartRate_NoAlert() {
        addRecord(70); // в норме

        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    @Test
    void testIrregularHeartRateDetected() {
        addRecord(80);
        addRecord(95);
        addRecord(60);
        addRecord(100);
        addRecord(70); // разнобой

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Irregular"));
    }

    @Test
    void testTooFewRecords_NoIrregularDetection() {
        addRecord(70);
        addRecord(68);
        addRecord(69); // < 5 измерений

        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    private void addRecord(double bpm) {
        patient.addRecord(bpm, "HeartRate", now);
        now += 10000; // +10 сек для следующей записи
    }
}
