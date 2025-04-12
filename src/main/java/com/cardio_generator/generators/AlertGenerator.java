package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Generates alert data for patients in the health data simulation.
 * <p>
 * This class is responsible for generating alerts for patients based on a probabilistic model.
 * Each patient has an alert state, which can be either "resolved" or "triggered".
 * If an alert is triggered, the alert state for that patient is set to "triggered".
 * After a certain period, there is a 90% chance that the alert will be resolved.
 * </p>
 * <p>
 * The alerts are generated with an average frequency, controlled by the variable 'Lambda', which represents the average
 * rate of alert generation. The class outputs the alert status ("triggered" or "resolved") via the provided {@link OutputStrategy}.
 * </p>
 *
 * @author Oryna Yukhymenko
 * @author Elena Gostiukhina
 */

public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();
    private boolean[] AlertStates; // false = resolved, true = pressed
    /**
     * Constructs an {@link AlertGenerator} instance for a given number of patients.
     * <p>
     * The constructor initializes the alert states for each patient. Each patient starts with a "resolved" alert state (false).
     * </p>
     *
     * @param patientCount The number of patients for whom alerts will be generated.
     *                     This determines the size of the {@code AlertStates} array.
     */

    public AlertGenerator(int patientCount) {
        AlertStates = new boolean[patientCount + 1];
    }
    /**
     * Generates and outputs an alert for the specified patient.
     * <p>
     * This method checks the alert state for the patient and either triggers a new alert or resolves an existing one.
     * If the alert is triggered, it outputs the alert with the status "triggered". If the alert is resolved, it outputs
     * the alert with the status "resolved".
     * </p>
     *
     * @param patientId The unique identifier for the patient whose alert is being generated.
     *                  This is used to look up and update the alert state for the specific patient.
     * @param outputStrategy The strategy for outputting the alert data. This will define how the data is output (e.g., to console, file, etc.).
     *                       The method uses this to send the alert data (status) to the appropriate output destination.
     * @throws Exception If an error occurs while generating or outputting the alert data, an exception is thrown.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (AlertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    AlertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double Lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-Lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    AlertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
