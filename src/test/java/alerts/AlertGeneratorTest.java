package alerts;


import com.alerts.AlertGenerator;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AlertGeneratorTest {
    private AlertGenerator alertGenerator;
    private Patient testPatient;
    private long currentTime;

    @BeforeEach
    void setUp() {
        alertGenerator = new AlertGenerator();
        testPatient = new Patient(1);
        currentTime = System.currentTimeMillis();
    }

    @Test
    void testCriticalBloodPressureAlert() {
        // Add critical BP records
        testPatient.addRecord(185, "SystolicPressure", currentTime - 5000);
        testPatient.addRecord(125, "DiastolicPressure", currentTime - 5000);

        // Capture System.out output
        ConsoleOutputCaptor captor = new ConsoleOutputCaptor();
        captor.start();

        alertGenerator.evaluateData(testPatient);

        String output = captor.stop();
        assertTrue(output.contains("Critical BP: 185.0/125.0 mmHg"));
    }

    @Test
    void testLowSaturationAlert() {
        testPatient.addRecord(91, "Saturation", currentTime - 3000);

        ConsoleOutputCaptor captor = new ConsoleOutputCaptor();
        captor.start();

        alertGenerator.evaluateData(testPatient);

        String output = captor.stop();
        assertTrue(output.contains("Low Oxygen: 91.0%"));
    }



    @Test
    void testECGAbnormality() {
        // Add ECG records with one abnormal value
        for (int i = 0; i < 30; i++) {
            testPatient.addRecord(1.0, "ECG", currentTime - (30 - i) * 1000);
        }
        testPatient.addRecord(5.0, "ECG", currentTime); // Abnormal value

        ConsoleOutputCaptor captor = new ConsoleOutputCaptor();
        captor.start();

        alertGenerator.evaluateData(testPatient);

        String output = captor.stop();
        assertTrue(output.contains("ECG Abnormality: 5.0"));
    }

    @Test
    void testHypotensiveHypoxemia() {
        testPatient.addRecord(85, "SystolicPressure", currentTime - 5000);
        testPatient.addRecord(90, "Saturation", currentTime - 4000);

        ConsoleOutputCaptor captor = new ConsoleOutputCaptor();
        captor.start();

        alertGenerator.evaluateData(testPatient);

        String output = captor.stop();
        assertTrue(output.contains("Hypotensive Hypoxemia"));
    }

    // Helper class to capture console output
    private static class ConsoleOutputCaptor {
        private final java.io.ByteArrayOutputStream outContent;
        private final java.io.PrintStream originalOut;

        public ConsoleOutputCaptor() {
            this.outContent = new java.io.ByteArrayOutputStream();
            this.originalOut = System.out;
        }

        public void start() {
            System.setOut(new java.io.PrintStream(outContent));
        }

        public String stop() {
            System.setOut(originalOut);
            return outContent.toString();
        }
    }
}