package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseDataDtoTest {

    @Test
    public void getCaseDataDto() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper, caseDeadline,caseReceived);

        CaseDataDto caseDataDto = CaseDataDto.from(caseData);

        assertThat(caseDataDto.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(caseDataDto.getCreated()).isEqualTo(caseData.getCreated());
        assertThat(caseDataDto.getType()).isEqualTo(caseData.getType());
        assertThat(caseDataDto.getReference()).isEqualTo(caseData.getReference());
        assertThat(caseDataDto.getData()).isEqualTo(caseData.getData());
        assertThat(caseDataDto.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(caseDataDto.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(caseDataDto.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(caseDataDto.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

    @Test
    public void getCaseDataDtoNull() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, objectMapper, caseDeadline, caseReceived);

        CaseDataDto caseDataDto = CaseDataDto.from(caseData);

        assertThat(caseDataDto.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(caseDataDto.getCreated()).isEqualTo(caseData.getCreated());
        assertThat(caseDataDto.getType()).isEqualTo(caseData.getType());
        assertThat(caseDataDto.getReference()).isEqualTo(caseData.getReference());
        assertThat(caseDataDto.getData()).isEqualTo(caseData.getData());
        assertThat(caseDataDto.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(caseDataDto.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(caseDataDto.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

    }

}