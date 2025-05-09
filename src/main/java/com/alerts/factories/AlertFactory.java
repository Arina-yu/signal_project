package com.alerts.factories;

import com.alerts.Alert;

public abstract class AlertFactory {

    /**
     * Creates an Alert object based on the provided parameters.
     *
     * @param patientId The ID of the patient associated with the alert
     * @param condition The condition that triggered the alert
     * @param timestamp The time when the alert was triggered
     * @return An Alert object specific to the factory implementation
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}