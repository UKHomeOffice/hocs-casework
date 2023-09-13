package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseDataTest {

    @Test
    public void getCaseData() {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isNull();
        assertThat(caseData.getPrimaryTopicUUID()).isNull();
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateReceived()).isEqualTo(caseReceived);
        assertThat(caseData.getDateCompleted()).isNull();
    }

    @Test
    public void getCaseDataSetDeadline() {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isEqualTo(caseDeadline);
        assertThat(caseData.getDateReceived()).isEqualTo(caseReceived);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseDataNullCaseDataTypeInside() {
        CaseDataType type = CaseDataTypeFactory.from(null, null);
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateReceived()).isEqualTo(caseReceived);
    }

    @Test
    public void getCaseDataReferenceFormat() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        long caseNumber = 1234L;

        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getReference()).matches("[A-Z]{3,4}/[0-9]{7}/[0-9]{2}");

        assertThat(caseData.getReference()).startsWith(type.getDisplayCode());
        assertThat(caseData.getReference()).contains(String.valueOf(caseNumber));
        assertThat(caseData.getReference()).endsWith(String.valueOf(caseData.getCreated().getYear()).substring(2, 4));

        // We encode the type into the UUID for quicker lookup (assuming type data is cached)
        assertThat(caseData.getUuid().toString()).endsWith(type.getShortCode());
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseDataNullType() {

        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        new CaseData(null, caseNumber, data, caseReceived);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseDataNullNumber() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        new CaseData(type, null, data, caseReceived);
    }

    @Test
    public void update() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateReceived()).isEqualTo(caseReceived);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData);

        assertThat(caseData.getDataMap().containsKey("new")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyValue")).isTrue();

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateReceived()).isEqualTo(caseReceived);
    }

    @Test
    public void updateWithExistingData() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("old", "anyOldValue");
        LocalDate caseReceived = LocalDate.now();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData);

        assertThat(caseData.getDataMap().containsKey("old")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyOldValue")).isTrue();
        assertThat(caseData.getDataMap().containsKey("new")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyValue")).isTrue();
    }

    @Test
    public void updateWithOverwriteExistingData() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");
        LocalDate caseReceived = LocalDate.now();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        Map<String, String> newData = new HashMap<>();
        newData.put("new", "anyValue");

        caseData.update(newData);

        assertThat(caseData.getDataMap().containsKey("new")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyValue")).isTrue();
    }

    @Test
    public void updateWithEmptyData() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");
        LocalDate caseReceived = LocalDate.now();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        caseData.update(new HashMap<>());

        assertThat(caseData.getDataMap().containsKey("new")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyOldValue")).isTrue();
    }

    @Test
    public void updateWithNullData() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("new", "anyOldValue");
        LocalDate caseReceived = LocalDate.now();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        caseData.update(null);

        assertThat(caseData.getDataMap().containsKey("new")).isTrue();
        assertThat(caseData.getDataMap().containsValue("anyOldValue")).isTrue();
    }

    @Test
    public void shouldComplete() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateCompleted()).isNull();

        caseData.setDateCompleted(LocalDateTime.now());

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
        assertThat(caseData.getDateCompleted()).isNotNull();
    }

    @Test
    public void shouldDelete() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();

        caseData.setDeleted(true);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isTrue();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();
    }

    @Test
    public void shouldSetPrimaryCorrespondent() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        LocalDate caseReceived = LocalDate.now();
        UUID primary = UUID.randomUUID();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();

        caseData.setPrimaryCorrespondentUUID(primary);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(primary);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();

    }

    @Test
    public void shouldSetPrimaryTopicUUID() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();

        LocalDate caseReceived = LocalDate.now();
        UUID primary = UUID.randomUUID();

        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(null);
        assertThat(caseData.getCaseDeadline()).isNull();

        caseData.setPrimaryTopicUUID(primary);

        assertThat(caseData.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseData.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseData.getType()).isEqualTo(type.getDisplayCode());
        assertThat(caseData.getDataMap()).isEqualTo(Map.of());
        assertThat(caseData.isDeleted()).isFalse();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isEqualTo(null);
        assertThat(caseData.getPrimaryTopicUUID()).isEqualTo(primary);
        assertThat(caseData.getCaseDeadline()).isNull();

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getShouldEnforceMigratedReferenceForMigrationCases() {

        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        LocalDateTime caseCreated= LocalDateTime.now();
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        new CaseData(type, caseNumber,data, caseReceived, caseCreated, null);
    }

}
