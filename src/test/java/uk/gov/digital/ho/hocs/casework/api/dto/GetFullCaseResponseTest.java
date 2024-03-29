package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GetFullCaseResponseTest {

    @Test
    public void getFullCaseDataDto() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setDateCompleted(LocalDateTime.now());

        GetCaseResponse getCaseResponse = GetCaseResponse.from(caseData, true);

        assertThat(getCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(getCaseResponse.getCreated().toLocalDateTime()).isEqualTo(caseData.getCreated());
        assertThat(getCaseResponse.getType()).isEqualTo(caseData.getType());
        assertThat(getCaseResponse.getReference()).isEqualTo(caseData.getReference());
        assertThat(getCaseResponse.getData()).isEqualTo(caseData.getDataMap());
        assertThat(getCaseResponse.getPrimaryTopic()).isEqualTo(caseData.getPrimaryTopicUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getPrimaryCorrespondent()).isEqualTo(caseData.getPrimaryCorrespondentUUID());
        assertThat(getCaseResponse.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(getCaseResponse.getCompleted()).isEqualTo(true);

    }

}
