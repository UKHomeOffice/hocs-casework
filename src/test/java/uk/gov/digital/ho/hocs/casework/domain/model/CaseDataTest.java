package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseDataTest {

    @Test
    public void getCaseData() {
        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

    }

    @Test
    public void getCaseDataReferenceFormat() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        long caseNumber = 1234L;

        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getReference()).matches("[A-Z]{3,4}/[0-9]{7}/[0-9]{2}");

        assertThat(caseData.getReference()).startsWith(type.getDisplayCode());
        assertThat(caseData.getReference()).contains(String.valueOf(caseNumber));
        assertThat(caseData.getReference()).endsWith(String.valueOf(caseData.getCreated().getYear()).substring(2, 4));

        // We encode the type into the UUID for quicker lookup (assuming type data is cached)
        assertThat(caseData.getUuid().toString()).endsWith(type.getShortCode());
    }

    @Test(expected = EntityCreationException.class)
    public void getCaseDataNullType() {

        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        new CaseData(null, caseNumber, data, objectMapper);
    }

    @Test(expected = EntityCreationException.class)
    public void getCaseDataNullNumber() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        new CaseData(type, null, data, objectMapper);
    }

    @Test
    public void update() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData, objectMapper);

        assertThat(caseData.getData().contains("new")).isTrue();
        assertThat(caseData.getData().contains("anyValue")).isTrue();

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
    }


    @Test
    public void updateWithExistingData() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("old", "anyOldValue");

        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData, objectMapper);

        assertThat(caseData.getData().contains("old")).isTrue();
        assertThat(caseData.getData().contains("anyOldValue")).isTrue();
        assertThat(caseData.getData().contains("new")).isTrue();
        assertThat(caseData.getData().contains("anyValue")).isTrue();
    }

    @Test
    public void updateWithOverwriteExistingData() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");

        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData, objectMapper);

        assertThat(caseData.getData().contains("new")).isTrue();
        assertThat(caseData.getData().contains("anyValue")).isTrue();
    }

    @Test
    public void updateWithEmptyData() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");

        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        Map<String, String> newData = new HashMap<>();

        caseData.update(newData, objectMapper);

        assertThat(caseData.getData().contains("new")).isTrue();
        assertThat(caseData.getData().contains("anyOldValue")).isTrue();
    }

    @Test
    public void updateWithNullData() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");

        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        caseData.update(null, objectMapper);

        assertThat(caseData.getData().contains("new")).isTrue();
        assertThat(caseData.getData().contains("anyOldValue")).isTrue();
    }

    @Test
    public void shouldUpdatePriority() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

        caseData.setPriority(true);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(true);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

    }

    @Test
    public void shouldSetPrimaryCorrespondent() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();

        UUID primary = UUID.randomUUID();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

        caseData.setPrimaryCorrespondentUUID(primary);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(primary);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

    }

    @Test
    public void updatePriority() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();

        UUID primary = UUID.randomUUID();

        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);

        caseData.setPrimaryTopicUUID(primary);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getData()).isEqualTo("{}");
        assertThat(caseData.isPriority()).isEqualTo(false);
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(primary);

    }


}