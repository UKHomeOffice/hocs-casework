package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.io.IOException;
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
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper,caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse getCaseResponse = GetCaseResponse.from(caseData, false);

        assertThat(getCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(getCaseResponse.getCreated().toLocalDateTime()).isEqualTo(caseData.getCreated());
        assertThat(getCaseResponse.getType()).isEqualTo(caseData.getType());
        assertThat(getCaseResponse.getReference()).isEqualTo(caseData.getReference());
        assertThat(getCaseResponse.getData()).isEqualTo(caseData.getData());
        assertThat(getCaseResponse.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

    @Test
    public void caseDataUUIDSubstitution() throws IOException {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;

        Map<String, String> data = new HashMap<>();
        data.put("Key1","Description 1");
        data.put("Key2","2d0904b2-123a-456b-789c-d6dbac804e72");
        data.put("2d0904b2-123a-456b-789c-d6dbac804e72","Description 2");
        data.put("Key3","3abcdef3-123a-456b-789c-d6dbac804e73");

        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper,caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse caseResponse = GetCaseResponse.from(caseData, true);
        String responseData = caseResponse.getData();
        Map<String, String> dataMap = getDataMap(responseData, objectMapper);

        assertThat("Description 1").isEqualTo(dataMap.get("Key1"));
        assertThat("Description 2").isEqualTo(dataMap.get("Key2"));
        assertThat("3abcdef3-123a-456b-789c-d6dbac804e73").isEqualTo(dataMap.get("Key3"));

        caseResponse = GetCaseResponse.from(caseData, false);
        responseData = caseResponse.getData();
        dataMap = getDataMap(responseData, objectMapper);

        assertThat("Description 1").isEqualTo(dataMap.get("Key1"));
        assertThat("2d0904b2-123a-456b-789c-d6dbac804e72").isEqualTo(dataMap.get("Key2"));
        assertThat("3abcdef3-123a-456b-789c-d6dbac804e73").isEqualTo(dataMap.get("Key3"));
    }

    private static Map<String, String> getDataMap(String dataString, ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(dataString, new TypeReference<Map<String, String>>() {});
    }


    @Test
    public void getCaseDataDtoNull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, objectMapper, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetCaseResponse getCaseResponse = GetCaseResponse.from(caseData, false);

        assertThat(getCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(getCaseResponse.getCreated().toLocalDateTime()).isEqualTo(caseData.getCreated());
        assertThat(getCaseResponse.getType()).isEqualTo(caseData.getType());
        assertThat(getCaseResponse.getReference()).isEqualTo(caseData.getReference());
        assertThat(getCaseResponse.getData()).isEqualTo(caseData.getData());
        assertThat(getCaseResponse.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

}