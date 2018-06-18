package uk.gov.digital.ho.hocs.casework.rsh.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateRshCaseRequestTest {

    @Test
    public void testCreateWithNoEntities() {
        CreateRshCaseRequest createRshCaseRequest = new CreateRshCaseRequest();

        assertThat(createRshCaseRequest.getSendEmailRequest()).isNull();
        assertThat(createRshCaseRequest.getCaseData()).isEmpty();
    }
}
