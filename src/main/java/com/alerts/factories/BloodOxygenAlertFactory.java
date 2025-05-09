package com.alerts.factories;

import com.alerts.Alert;

public class BloodOxygenAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        // Create specific blood oxygen alert with additional properties if needed
        return new Alert(patientId, "Blood Oxygen Alert: " + condition, timestamp);
    }
}
