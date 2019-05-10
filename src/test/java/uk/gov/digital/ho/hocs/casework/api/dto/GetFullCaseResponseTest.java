package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class GetFullCaseResponseTest {

    @Test
    public void getFullCaseDataDto() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper, caseReceived);
        caseData.setCaseDeadline(caseDeadline);

        GetFullCaseResponse getCaseResponse = GetFullCaseResponse.from(caseData);

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

}