package alerts;


import com.alerts.Alert;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    class AlertFactoriesTest {

        @Test
        void testBloodPressureAlertFactory() {
            BloodPressureAlertFactory factory = new BloodPressureAlertFactory();
            Alert alert = factory.createAlert("123", "High BP", 1000L);

            assertEquals("123", alert.getPatientId());
            assertEquals("Blood Pressure Alert: High BP", alert.getCondition());
            assertEquals(1000L, alert.getTimestamp());
        }

        @Test
        void testBloodOxygenAlertFactory() {
            BloodOxygenAlertFactory factory = new BloodOxygenAlertFactory();
            Alert alert = factory.createAlert("456", "Low O2", 2000L);

            assertEquals("456", alert.getPatientId());
            assertEquals("Blood Oxygen Alert: Low O2", alert.getCondition());
            assertEquals(2000L, alert.getTimestamp());
        }

        @Test
        void testECGAlertFactory() {
            ECGAlertFactory factory = new ECGAlertFactory();
            Alert alert = factory.createAlert("789", "Abnormal rhythm", 3000L);

            assertEquals("789", alert.getPatientId());
            assertEquals("ECG Alert: Abnormal rhythm", alert.getCondition());
            assertEquals(3000L, alert.getTimestamp());
        }
    }

