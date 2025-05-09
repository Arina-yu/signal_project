package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for creating ECG related alerts.
 */
public class ECGAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        // Create specific ECG alert with additional properties if needed
        return new Alert(patientId, "ECG Alert: " + condition, timestamp);
    }
}