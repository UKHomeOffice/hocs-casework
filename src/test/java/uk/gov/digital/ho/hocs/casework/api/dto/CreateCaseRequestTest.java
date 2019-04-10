package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseRequestTest {

    @Test
    public void getCreateCaseRequest() {

        CaseDataType caseDataType = new CaseDataType("MIN", "a1");
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(caseDataType.getDisplayCode(), data, caseReceived);

        assertThat(createCaseRequest.getType()).isEqualTo(caseDataType.getDisplayCode());
        assertThat(createCaseRequest.getData()).isEqualTo(data);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(null, null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getData()).isNull();
    }

}