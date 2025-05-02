


    package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

    class PatientTest {
        private Patient patient;
        private final int PATIENT_ID = 123;

        @BeforeEach
        void setUp() {
            patient = new Patient(PATIENT_ID);

            // Add some test records with different timestamps
            patient.addRecord(72.5, "HeartRate", 1000L);  // before range
            patient.addRecord(75.0, "HeartRate", 2000L);  // start of range
            patient.addRecord(120.0, "BloodPressure", 2500L);  // within range
            patient.addRecord(78.0, "HeartRate", 3000L);  // within range
            patient.addRecord(125.0, "BloodPressure", 4000L);  // end of range
            patient.addRecord(80.0, "HeartRate", 5000L);  // after range
        }

        @Test
        void testGetRecords_WithinRange() {
            List<PatientRecord> records = patient.getRecords(2000L, 4000L);

            assertEquals(4, records.size(), "Should return 4 records within the range");
            assertEquals(75.0, records.get(0).getMeasurementValue());
            assertEquals(120.0, records.get(1).getMeasurementValue());
            assertEquals(78.0, records.get(2).getMeasurementValue());
            assertEquals(125.0, records.get(3).getMeasurementValue());
        }

        @Test
        void testGetRecords_ExactBoundaries() {
            List<PatientRecord> records = patient.getRecords(2000L, 2000L);

            assertEquals(1, records.size(), "Should return only records at exact start time");
            assertEquals(75.0, records.get(0).getMeasurementValue());
        }

        @Test
        void testGetRecords_NoRecordsInRange() {
            List<PatientRecord> records = patient.getRecords(6000L, 7000L);
            assertTrue(records.isEmpty(), "Should return empty list when no records in range");
        }

        @Test
        void testGetRecords_AllRecordsInRange() {
            List<PatientRecord> records = patient.getRecords(0L, 6000L);
            assertEquals(6, records.size(), "Should return all records when range covers all");
        }

        @Test
        void testGetRecords_StartTimeAfterEndTime() {
            List<PatientRecord> records = patient.getRecords(4000L, 2000L);
            assertTrue(records.isEmpty(), "Should return empty list when start time is after end time");
        }

        @Test
        void testGetRecords_EmptyPatient() {
            Patient emptyPatient = new Patient(456);
            List<PatientRecord> records = emptyPatient.getRecords(0L, 10000L);
            assertTrue(records.isEmpty(), "Should return empty list for patient with no records");
        }

        @Test
        void testGetRecords_SingleRecordAtStartTime() {
            List<PatientRecord> records = patient.getRecords(2000L, 2001L);
            assertEquals(1, records.size(), "Should return single record at start time");
            assertEquals(75.0, records.get(0).getMeasurementValue());
        }

        @Test
        void testGetRecords_SingleRecordAtEndTime() {
            List<PatientRecord> records = patient.getRecords(3999L, 4000L);
            assertEquals(1, records.size(), "Should return single record at end time");
            assertEquals(125.0, records.get(0).getMeasurementValue());
        }

        @Test
        void testGetRecords_RecordsWithSameTimestamp() {
            // Add two records with identical timestamps
            patient.addRecord(90.0, "OxygenSaturation", 2500L);
            patient.addRecord(95.0, "OxygenSaturation", 2500L);

            List<PatientRecord> records = patient.getRecords(2500L, 2500L);
            assertEquals(3, records.size(), "Should return all records with same timestamp");
        }

        @Test
        void testGetRecords_VerifyPatientIdConsistency() {
            List<PatientRecord> records = patient.getRecords(0L, 10000L);
            for (PatientRecord record : records) {
                assertEquals(PATIENT_ID, record.getPatientId(),
                        "All returned records should belong to the same patient");
            }
        }
    }

