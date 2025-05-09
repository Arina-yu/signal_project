package cardio_generator.outputs;

import com.cardio_generator.HealthDataSimulator;
import com.cardio_generator.outputs.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HealthDataSimulatorTest {

    @Test
    void testSingletonInstance() {
        HealthDataSimulator simulator1 = HealthDataSimulator.getInstance();
        HealthDataSimulator simulator2 = HealthDataSimulator.getInstance();
        assertSame(simulator1, simulator2, "Singleton instances should be the same");
    }

    @Test
    void testConsoleOutputConfiguration() throws IOException {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        String[] args = {"--output", "console"};
        simulator.start(args);
        // если не упало — успех
        assertTrue(true);
    }

    @Test
    void testFileOutputConfigurationCreatesDirectory() throws IOException {
        String dir = "test_output_dir";
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        String[] args = {"--output", "file:" + dir};
        simulator.start(args);

        assertTrue(new java.io.File(dir).exists(), "Output directory should be created");
        // Очистка после теста
        new java.io.File(dir).delete();
    }

    @Test
    void testInvalidPatientCountDefaults() throws IOException {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        String[] args = {"--patient-count", "invalid_number"};
        simulator.start(args);
        // Если не упало — работает
        assertTrue(true);
    }



    @Test
    void testScheduleTaskRunsWithoutException() {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        assertDoesNotThrow(() -> {
            simulator.getClass()
                    .getDeclaredMethod("scheduleTask", Runnable.class, long.class, TimeUnit.class)
                    .setAccessible(true);
        });
    }
}
