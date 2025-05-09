package cardio_generator.outputs;

import com.cardio_generator.outputs.WebSocketOutputStrategy;
import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketOutputStrategyIntegrationTest {

    private WebSocketOutputStrategy outputStrategy;
    private TestWebSocketClient testClient;
    private static final int TEST_PORT = 8887;

    @BeforeEach
    void setUp() throws Exception {
        // Создаем реальный сервер
        outputStrategy = new WebSocketOutputStrategy(TEST_PORT);

        // Даем серверу время на запуск
        Thread.sleep(500);

        // Создаем тестового клиента
        testClient = new TestWebSocketClient(new URI("ws://localhost:" + TEST_PORT));
        testClient.connectBlocking(1, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (testClient != null) {
            testClient.close();
        }
        if (outputStrategy != null) {
            outputStrategy.getServer().stop();
        }
        Thread.sleep(300); // Даем время на завершение
    }

    @Test
    void output_ShouldSendMessageToConnectedClient() throws Exception {
        // Ожидаемое сообщение
        String expectedMessage = "1,123456789,HeartRate,72";
        CountDownLatch latch = new CountDownLatch(1);
        testClient.setLatch(latch);

        // Отправляем сообщение через стратегию
        outputStrategy.output(1, 123456789L, "HeartRate", "72");

        // Ждем получения сообщения (макс 2 секунды)
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Сообщение не было получено");
        assertEquals(expectedMessage, testClient.getLastMessage());
    }

    @Test
    void output_WithNoConnections_ShouldNotFail() {
        // Закрываем клиента
        testClient.close();

        // Должно выполниться без ошибок
        assertDoesNotThrow(() ->
                outputStrategy.output(1, 123456789L, "BloodPressure", "120/80")
        );
    }

    // Простой WebSocket клиент для тестирования
    private static class TestWebSocketClient extends org.java_websocket.client.WebSocketClient {
        private String lastMessage;
        private CountDownLatch latch;

        public TestWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(org.java_websocket.handshake.ServerHandshake handshakedata) {
        }

        @Override
        public void onMessage(String message) {
            this.lastMessage = message;
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }
}