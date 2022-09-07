package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCaseResponseTest {

    @Test
    public void getCaseDataDto() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse getCaseResponse = GetCaseResponse.from(caseData, false);

        assertThat(getCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(getCaseResponse.getCreated().toLocalDateTime()).isEqualTo(caseData.getCreated());
        assertThat(getCaseResponse.getType()).isEqualTo(caseData.getType());
        assertThat(getCaseResponse.getReference()).isEqualTo(caseData.getReference());
        assertThat(getCaseResponse.getData()).isEqualTo(caseData.getDataMap());
        assertThat(getCaseResponse.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

    @Test
    public void caseDataUUIDSubstitutionNotFull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;

        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Description 1");
        data.put("Key2", "2d0904b2-123a-456b-789c-d6dbac804e72");
        data.put("2d0904b2-123a-456b-789c-d6dbac804e72", "Description 2");
        data.put("Key3", "3abcdef3-123a-456b-789c-d6dbac804e73");

        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse caseResponse = GetCaseResponse.from(caseData, true);
        Map<String, String> dataMap = caseResponse.getData();

        assertThat("Description 1").isEqualTo(dataMap.get("Key1"));
        assertThat("Description 2").isEqualTo(dataMap.get("Key2"));
        assertThat("3abcdef3-123a-456b-789c-d6dbac804e73").isEqualTo(dataMap.get("Key3"));

    }

    @Test
    public void caseDataUUIDSubstitutionFull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;

        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Description 1");
        data.put("Key2", "2d0904b2-123a-456b-789c-d6dbac804e72");
        data.put("2d0904b2-123a-456b-789c-d6dbac804e72", "Description 2");
        data.put("Key3", "3abcdef3-123a-456b-789c-d6dbac804e73");

        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse caseResponse = GetCaseResponse.from(caseData, false);
        Map<String, String> dataMap = caseResponse.getData();

        assertThat("Description 1").isEqualTo(dataMap.get("Key1"));
        assertThat("2d0904b2-123a-456b-789c-d6dbac804e72").isEqualTo(dataMap.get("Key2"));
        assertThat("3abcdef3-123a-456b-789c-d6dbac804e73").isEqualTo(dataMap.get("Key3"));
    }

    @Test
    public void getCaseDataDtoNull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse getCaseResponse = GetCaseResponse.from(caseData, false);

        assertThat(getCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(getCaseResponse.getCreated().toLocalDateTime()).isEqualTo(caseData.getCreated());
        assertThat(getCaseResponse.getType()).isEqualTo(caseData.getType());
        assertThat(getCaseResponse.getReference()).isEqualTo(caseData.getReference());
        assertThat(getCaseResponse.getData()).isEqualTo(caseData.getDataMap());
        assertThat(getCaseResponse.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

}
