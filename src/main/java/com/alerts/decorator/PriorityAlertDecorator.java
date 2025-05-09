package com.alerts.decorator;

import com.alerts.Alert;

/**
 * Decorator that adds priority tagging to alerts.
 * Allows dynamic prioritization of alerts without modifying original classes.
 */
public class PriorityAlertDecorator extends AlertDecorator {
    /**
     * Priority levels for alerts
     */
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private final Priority priority;

    /**
     * Creates a prioritized alert
     * @param decoratedAlert the alert to decorate
     * @param priority the priority level to assign
     */
    public PriorityAlertDecorator(Alert decoratedAlert, Priority priority) {
        super(decoratedAlert);
        this.priority = priority;
    }

    /**
     * Gets the decorated condition with priority
     * @return modified condition message
     */
    @Override
    public String getCondition() {
        return "[" + priority + " PRIORITY] " + super.getCondition();
    }

    /**
     * Gets the alert's priority level
     * @return priority enum value
     */
    public Priority getPriority() {
        return priority;
    }
}