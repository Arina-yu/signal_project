package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;

public interface AlertStrategy {
    Alert checkAlert(Patient patient);
}
