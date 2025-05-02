package data_management;

import com.data_management.DataReaderClass;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataReaderClassTest {

    private DataReaderClass reader;
    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("testdata").toFile();
        reader = new DataReaderClass(tempDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        for (File file : Objects.requireNonNull(tempDir.listFiles())) {
            file.delete();
        }
        tempDir.delete();
    }



    @Test
    void testReadDataWithInvalidJsonFileThrowsIOException() throws IOException {
        File badFile = new File(tempDir, "bad.json");
        try (FileWriter writer = new FileWriter(badFile)) {
            writer.write("{ invalid json ...");
        }

        DataStorage storage = new DataStorage();

        IOException exception = assertThrows(IOException.class, () -> {
            reader.readData(storage);
        });

        assertTrue(exception.getMessage().contains("Failed to process file"));
    }

    @Test
    void testReadDataWithNullStorageThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            reader.readData(null);
        });
    }

    @Test
    void testParseRecordWithValidJson() {
        JSONObject json = new JSONObject(Map.of(
                "patientId", 1,
                "recordType", "BloodPressure",
                "measurementValue", 120.5,
                "timestamp", 1700000000123L
        ));

        PatientRecord record = reader.parseRecord(json);

        assertNotNull(record);
        assertEquals(1, record.getPatientId());
        assertEquals("BloodPressure", record.getRecordType());
    }

    @Test
    void testParseRecordWithMissingFieldReturnsNull() {
        JSONObject json = new JSONObject(Map.of(
                "patientId", 1,
                "recordType", "BloodPressure"
        ));

        PatientRecord record = reader.parseRecord(json);

        assertNull(record);
    }

    @Test
    void testStoreRecordsAddsToStorage() {
        DataStorage storage = new DataStorage();
        List<PatientRecord> records = List.of(
                new PatientRecord(5, 98.6, "Temperature", 1700001234567L)
        );

        reader.storeRecords(storage, records);

        List<PatientRecord> stored = storage.getRecords(5, 0, Long.MAX_VALUE);
        assertEquals(1, stored.size());
        assertEquals("Temperature", stored.get(0).getRecordType());
    }
}
