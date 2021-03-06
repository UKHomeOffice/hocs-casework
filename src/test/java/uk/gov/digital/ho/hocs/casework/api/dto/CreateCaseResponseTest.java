package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseResponseTest {

    @Test
    public void getCreateCaseResponse() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper,caseReceived);

        CreateCaseResponse createCaseResponse = CreateCaseResponse.from(caseData);

        assertThat(createCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(createCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

    @Test
    public void getCreateCaseResponseNull() {

        CaseDataType type = new CaseDataType("MIN", "a1");
        Long caseNumber = 1234L;

        ObjectMapper objectMapper = new ObjectMapper();
        LocalDate caseDeadline = LocalDate.now().plusDays(20);
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, objectMapper,caseReceived);

        CreateCaseResponse createCaseResponse = CreateCaseResponse.from(caseData);

        assertThat(createCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(createCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

}