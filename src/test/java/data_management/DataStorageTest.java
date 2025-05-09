package data_management;


import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    private DataStorage dataStorage;

    @BeforeEach
    void setUp() {
        dataStorage = DataStorage.getInstance();

        // Cleaning up the staff
        List<Patient> allPatients = dataStorage.getAllPatients();
        for (Patient patient : allPatients) {
            // Assuming Patient class has a clearRecords() or similar for testing purposes
            patient.getRecords(0, Long.MAX_VALUE).clear(); // или переопределите DataStorage для тестов
        }
    }

    @Test
    void testSingletonInstance() {
        DataStorage anotherInstance = DataStorage.getInstance();
        assertSame(dataStorage, anotherInstance, "DataStorage should be a singleton");
    }

    @Test
    void testAddAndRetrievePatientData() {
        int patientId = 101;
        double value = 98.6;
        String type = "Temperature";
        long timestamp = System.currentTimeMillis();

        dataStorage.addPatientData(patientId, value, type, timestamp);
        List<PatientRecord> records = dataStorage.getRecords(patientId, timestamp - 1000, timestamp + 1000);

        assertEquals(1, records.size());
        PatientRecord record = records.get(0);
        assertEquals(patientId, record.getPatientId());
        assertEquals(type, record.getRecordType());
        assertEquals(value, record.getMeasurementValue());
    }

    @Test
    void testGetRecordsReturnsEmptyForUnknownPatient() {
        List<PatientRecord> records = dataStorage.getRecords(9999, 0, Long.MAX_VALUE);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetAllPatientsReturnsCorrectList() {
        int patientId = 222;
        dataStorage.addPatientData(patientId, 120.0, "BloodPressure", System.currentTimeMillis());

        List<Patient> allPatients = dataStorage.getAllPatients();
        boolean found = allPatients.stream().anyMatch(p -> p.getPatientId() == patientId);

        assertTrue(found, "Expected patient to be in the list of all patients");
    }
}

