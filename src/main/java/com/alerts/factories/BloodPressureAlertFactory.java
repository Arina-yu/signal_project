package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for creating blood pressure related alerts.
 */
public class BloodPressureAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        // Create specific blood pressure alert with additional properties if needed
        return new Alert(patientId, "Blood Pressure Alert: " + condition, timestamp);
    }
}