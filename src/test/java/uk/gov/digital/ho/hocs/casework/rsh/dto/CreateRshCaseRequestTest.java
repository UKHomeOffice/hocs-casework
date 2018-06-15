package uk.gov.digital.ho.hocs.casework.rsh.dto;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateRshCaseRequestTest {

    @Test
    public void testCreateWithNoEntities() {
        CreateRshCaseRequest createRshCaseRequest = new CreateRshCaseRequest();

        assertThat(createRshCaseRequest.getSendEmailRequest()).isNotNull();
        assertThat(createRshCaseRequest.getCaseData()).isEmpty();
    }

    @Test
    public void testCreateWithEntities() {
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();

        Map<String, String> caseData = new HashMap<>();
        caseData.put("key", "value");

        CreateRshCaseRequest createRshCaseRequest = new CreateRshCaseRequest(sendEmailRequest, caseData);

        assertThat(createRshCaseRequest.getSendEmailRequest()).isEqualTo(sendEmailRequest);
        assertThat(createRshCaseRequest.getCaseData()).isEqualTo(caseData);
    }
}
