package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Generates and simulates blood saturation data for patients in health monitoring simulations.
 * This class generates realistic blood saturation levels, simulating small fluctuations in the saturation
 * values over time. It ensures the values stay within a healthy range and outputs them via a specified
 * {@link OutputStrategy}.
 * <p>
 * The class is initialized with baseline saturation values for each patient, and it updates the saturation
 * values with slight random variations to simulate real-world changes in blood oxygen saturation.
 * </p>
 *
 * @author Elena and Oryna
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;
    /**
     * Constructs a {@link BloodSaturationDataGenerator} for a given number of patients.
     * Initializes each patient's blood saturation with a random value between 95 and 100 percent.
     *
     * @param patientCount The number of patients for which baseline saturation values will be generated.
     *                     Each patient is initialized with a random saturation value between 95% and 100%.
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }
    /**
     * Generates a blood saturation value for a given patient and outputs it using the specified output strategy.
     * The blood saturation value fluctuates slightly (within -1, 0, or 1) from the previous value to simulate real-time
     * changes in saturation levels. The generated value is then sent to the output strategy for further processing,
     * such as saving to a file or transmitting over a network.
     * <p>
     * The method ensures the blood saturation remains within the healthy range of 90% to 100%.
     * </p>
     *
     * @param patientId The unique identifier for the patient whose blood saturation data is being generated.
     * @param outputStrategy The strategy used to handle the output of the generated blood saturation data,
     *                       such as writing it to a file or sending it over a network.
     * @throws RuntimeException if any unexpected error occurs during data generation or output.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
