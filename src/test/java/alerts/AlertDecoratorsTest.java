package alerts;



import com.alerts.Alert;
import com.alerts.decorator.PriorityAlertDecorator;
import com.alerts.decorator.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlertDecoratorsTest {
    private final Alert basicAlert = new Alert("123", "Test Condition", 1000L);

    @Test
    void testPriorityDecorator() {
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(
                basicAlert,
                PriorityAlertDecorator.Priority.CRITICAL
        );

        assertEquals("[CRITICAL PRIORITY] Test Condition", decorated.getCondition());
        assertEquals(PriorityAlertDecorator.Priority.CRITICAL, decorated.getPriority());
    }


    @Test
    void testNestedDecorators() {
        Alert alert = new PriorityAlertDecorator(
                new RepeatedAlertDecorator(basicAlert, 5000, 1),
                PriorityAlertDecorator.Priority.HIGH
        );

        assertEquals("[HIGH PRIORITY] Test Condition [REPEATED 0x]", alert.getCondition());
    }
}