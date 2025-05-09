package com.alerts.decorator;

import com.alerts.Alert;
import com.alerts.decorator.AlertDecorator;

/**
 * Decorator that adds repeated alert functionality.
 * Triggers the alert multiple times at specified intervals until resolved.
 * Useful for persistent conditions that need ongoing attention.
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    private final int repeatInterval; // in milliseconds
    private final int maxRepeats;
    private int repeatCount = 0;
    private long lastTriggerTime = 0;

    /**
     * Creates a new repeated alert decorator
     * @param decoratedAlert the alert to decorate
     * @param repeatInterval how often to repeat (ms)
     * @param maxRepeats maximum number of repeats
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatInterval, int maxRepeats) {
        super(decoratedAlert);
        this.repeatInterval = repeatInterval;
        this.maxRepeats = maxRepeats;
    }

    /**
     * Checks if the alert should be repeated
     * @return true if the alert should trigger again
     */
    public boolean shouldRepeat() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTriggerTime >= repeatInterval && repeatCount < maxRepeats) {
            lastTriggerTime = currentTime;
            repeatCount++;
            return true;
        }
        return false;
    }

    /**
     * Gets the decorated condition with repeat count
     * @return modified condition message
     */
    @Override
    public String getCondition() {
        return super.getCondition() + " [REPEATED " + repeatCount + "x]";
    }
}