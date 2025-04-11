package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Interface that defines the contract for generating patient data in health monitoring simulations.
 * This interface is intended to be implemented by classes that generate various types of patient data,
 * such as vital signs, for health-related applications. The generated data will be output using a specified
 * {@link OutputStrategy}.
 *
 * @author Oryna and Elena
 */
public interface PatientDataGenerator {

    /**
     * Generates patient data for a given patient ID and outputs it using the specified output strategy.
     * This method is expected to create randomized or simulated data, which could include information
     * like heart rate, blood pressure, or other vital signs. The data will be sent to the output strategy
     * for processing, such as saving to a file or sending over a network.
     *
     * @param patientId The unique identifier for the patient whose data is being generated.
     * @param outputStrategy The strategy used to handle the output of the generated data, such as writing
     *                       it to a file or transmitting it over a network.
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
