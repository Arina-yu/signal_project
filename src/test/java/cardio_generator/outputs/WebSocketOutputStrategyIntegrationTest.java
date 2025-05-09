package cardio_generator.outputs;

import com.cardio_generator.outputs.WebSocketOutputStrategy;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketOutputStrategyIntegrationTest {

    private static final int PORT = 8887;
    private WebSocketOutputStrategy strategy;
    private TestClient client;
    private static final String WS_URL = "ws://localhost:" + PORT;

    @BeforeEach
    void setUp() throws Exception {
        // Запуск WebSocketOutputStrategy с реальным сервером
        strategy = new WebSocketOutputStrategy(PORT);

        // Подключение клиента
        client = new TestClient(new URI(WS_URL));
        client.connectBlocking(); // ждем подключения
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
        strategy.getServer().stop();
    }

    @Test
    void testOutputMessageReceivedByClient() throws Exception {
        int patientId = 42;
        long timestamp = 1700000000123L;
        String label = "HeartRate";
        String data = "78";

        String expectedMessage = patientId + "," + timestamp + "," + label + "," + data;

        strategy.output(patientId, timestamp, label, data);

        // Ждем сообщение от сервера (до 3 секунд)
        String received = client.awaitMessage(3, TimeUnit.SECONDS);

        assertNotNull(received);
        assertEquals(expectedMessage, received);
    }

    // Встроенный тестовый WebSocket клиент
    static class TestClient extends WebSocketClient {

        private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        public TestClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("Client connected");
        }

        @Override
        public void onMessage(String message) {
            messageQueue.offer(message); // кладем сообщение в очередь
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {}

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }

        public String awaitMessage(long timeout, TimeUnit unit) throws InterruptedException {
            return messageQueue.poll(timeout, unit);
        }
    }
}
