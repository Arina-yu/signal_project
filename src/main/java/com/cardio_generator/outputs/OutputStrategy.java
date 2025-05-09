package com.cardio_generator.outputs;

/**
 * Interface representing a strategy of outputting patient data.
 * Implementations of this interface define how data for a patient is
 * handled and where it is sent or stored.
 */

public interface OutputStrategy {

    /**
     * Outputs of data record for a patient.
     *
     * @param patientId  unique id for patient
     * @param timestamp  timestamp at which the data was generated
     * @param label      label indicating the type of data
     * @param data       actual data to be output
     */
    void output(int patientId, long timestamp, String label, String data);
}