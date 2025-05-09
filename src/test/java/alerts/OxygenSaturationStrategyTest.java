package alerts;

import com.alerts.Alert;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OxygenSaturationStrategyTest {
    private OxygenSaturationStrategy strategy;
    private Patient patient;
    private long now;

    @BeforeEach
    void setUp() {
        strategy = new OxygenSaturationStrategy();
        patient = new Patient(42);
        now = System.currentTimeMillis();
    }

    @Test
    void testCriticalLowSaturation() {
        addRecord(89, now - 5000); // < 92%

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Critical Low Oxygen Saturation"));
    }



    @Test
    void testRapidDropRateBased() {
        addRecord(97, now - 60000); // 1 min ago
        addRecord(96.4, now);       // Drop = 0.6 in 1 min -> 0.6 > 0.5

        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Fast Oxygen Desaturation"));
    }

    @Test
    void testNormalSaturation_NoAlert() {
        addRecord(96, now - 100000);
        addRecord(95.8, now); // Small drop, above critical

        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    @Test
    void testNotEnoughData_NoAlert() {
        addRecord(95, now); // Only one record

        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    private void addRecord(double value, long timestamp) {
        patient.addRecord(value, "Saturation", timestamp);
    }
}
