package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
public class JsonConfigFolderReaderTest {

    private ObjectMapper objectMapper;

    private JsonConfigFolderReader jsonConfigFolderReader;

    @Before
    public void setup() {
        this.objectMapper = new SpringConfiguration().initialiseObjectMapper();
    }

    @Test(expected = ApplicationExceptions.ConfigFolderReadException.class)
    public void shouldFailWithNonExistantFolder() {
        jsonConfigFolderReader = new JsonConfigFolderReader(objectMapper) {
            @Override
            String getFolderName() {
                return "TestNonExistant";
            }
        };

        jsonConfigFolderReader.readValueFromFolder(new TypeReference<TestClass>() {});
    }

    @Test(expected = ApplicationExceptions.ConfigFileReadException.class)
    public void shouldFailWithUnparsableFileExistantFolder() {
        jsonConfigFolderReader = new JsonConfigFolderReader(objectMapper) {
            @Override
            String getFolderName() {
                return "json/TestBad";
            }
        };

        jsonConfigFolderReader.readValueFromFolder(new TypeReference<TestClass>() {});
    }

    @Test
    public void shouldParseWithCorrectPojoFormat() {
        jsonConfigFolderReader = new JsonConfigFolderReader(objectMapper) {
            @Override
            String getFolderName() {
                return "json/TestGood";
            }
        };

        var value = jsonConfigFolderReader.readValueFromFolder(new TypeReference<TestClass>() {});
        assertEquals(2, value.get("Test").size());
    }

    public static class TestClass implements JsonConfigFolderReader.CaseTypeObject<List<String>> {

        private final String type;

        private final List<String> values;

        @JsonCreator
        public TestClass(@JsonProperty("type") String type, @JsonProperty("values") List<String> values) {
            this.type = type;
            this.values = values;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public List<String> getValue() {
            return values;
        }

    }

}
