package com.alerts.decorator;

import com.alerts.Alert;

/**
 * Base decorator class for alerts following the Decorator pattern.
 * Maintains a reference to the wrapped Alert object and delegates all calls to it.
 * This serves as the foundation for all alert decorators.
 */
public abstract class AlertDecorator extends Alert {
    protected final Alert decoratedAlert;

    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(),
                decoratedAlert.getCondition(),
                decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    /**
     * Gets the decorated alert's patient ID
     * @return patient ID string
     */
    @Override
    public String getPatientId() {
        return decoratedAlert.getPatientId();
    }

    /**
     * Gets the decorated alert's condition message
     * @return condition description
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition();
    }

    /**
     * Gets the decorated alert's timestamp
     * @return timestamp in milliseconds
     */
    @Override
    public long getTimestamp() {
        return decoratedAlert.getTimestamp();
    }
}