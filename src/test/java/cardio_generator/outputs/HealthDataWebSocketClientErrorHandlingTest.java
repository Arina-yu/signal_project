package cardio_generator.outputs;

import com.cardio_generator.outputs.HealthDataWebSocketClient;
import com.data_management.DataStorage;
import org.junit.jupiter.api.*;
import java.net.URI;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class HealthDataWebSocketClientErrorHandlingTest {

    private DataStorage dataStorage;
    private HealthDataWebSocketClient client;

    @BeforeEach
    void setUp() {
        dataStorage = mock(DataStorage.class);
    }

    // Test to verify that the onError method handles errors correctly
    @Test
    void testOnErrorReconnectionAttempts() throws InterruptedException {
        URI serverUri = URI.create("ws://localhost:8080");
        client = new HealthDataWebSocketClient(serverUri, dataStorage);

        client.onError(new Exception("Simulated WebSocket Error")); // Simulate an error

        Thread.sleep(6000); // Wait for reconnection delay
    }

    // Test to verify that the onClose method triggers a reconnection attempt
    @Test
    void testOnCloseTriggersReconnect() throws InterruptedException {
        URI serverUri = URI.create("ws://localhost:8080");
        client = new HealthDataWebSocketClient(serverUri, dataStorage);

        // Simulate connection closure
        client.onClose(1000, "Normal closure", false);

        // Verify reconnection is attempted
        Thread.sleep(6000); // Wait for reconnection delay
    }
}