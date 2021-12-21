package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseResponseTest {

    @Test
    public void getCreateCaseResponse() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data,caseReceived);

        CreateCaseResponse createCaseResponse = CreateCaseResponse.from(caseData);

        assertThat(createCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(createCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

    @Test
    public void getCreateCaseResponseNull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;

        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, caseReceived);

        CreateCaseResponse createCaseResponse = CreateCaseResponse.from(caseData);

        assertThat(createCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(createCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

}
