package cardio_generator.outputs;



import com.data_management.DataReaderClass;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataReaderClassTest {

    @TempDir
    Path tempDir;
    private DataReaderClass dataReader;
    private DataStorage mockDataStorage;

    @BeforeEach
    void setUp() {
        mockDataStorage = mock(DataStorage.class);
        dataReader = new DataReaderClass(tempDir.toString());
    }



    @Test
    void readData_WithInvalidJsonFile_ShouldLogError() throws IOException {
        // Arrange - create invalid JSON file
        File testFile = tempDir.resolve("invalid.json").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("invalid json");
        }

        // Act & Assert
        assertDoesNotThrow(() -> dataReader.readData(mockDataStorage));
    }

    @Test
    void readData_WithEmptyDirectory_ShouldNotFail() {
        assertDoesNotThrow(() -> dataReader.readData(mockDataStorage));
    }

    @Test
    void readData_WithNullDataStorage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> dataReader.readData(null));
    }







    @Test
    void parseRecord_WithMissingFields_ShouldReturnNull() {
        // Arrange
        JSONObject incompleteRecord = new JSONObject();
        incompleteRecord.put("patientId", 5);
        incompleteRecord.put("recordType", "HeartRate");
        // Missing measurementValue and timestamp

        // Act
        PatientRecord record = dataReader.parseRecord(incompleteRecord);

        // Assert
        assertNull(record);
    }
}
