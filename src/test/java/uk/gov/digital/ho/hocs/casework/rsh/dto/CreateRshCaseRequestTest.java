package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateRshCaseRequestTest {

    @Test
    public void testCreateWithNoEntities() {
        CreateRshCaseRequest createRshCaseRequest = new CreateRshCaseRequest();

        assertThat(createRshCaseRequest.getSendEmailRequest()).isNull();
        assertThat(createRshCaseRequest.getCaseData()).isEmpty();
    }

    @Test
    public void testCreateNull() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateRshCaseRequest createRshCaseRequest = objectMapper.readValue("{}", CreateRshCaseRequest.class);

        SendRshEmailRequest sendEmailRequest = objectMapper.readValue("{}", SendRshEmailRequest.class);
        Map<String, String> caseData = new HashMap<>();

        assertThat(createRshCaseRequest.getSendEmailRequest()).isNull();
        assertThat(createRshCaseRequest.getCaseData()).isEqualTo(caseData);
    }

    @Test
    public void testCreateWithEntities() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateRshCaseRequest createRshCaseRequest = objectMapper.readValue("{ \"sendEmailRequest\" : { \"email\" : \"a\", \"teamName\" : \"b\" }, \"caseData\" : { \"key\" : \"value\"} }", CreateRshCaseRequest.class);

        SendRshEmailRequest sendEmailRequest = objectMapper.readValue("{ \"email\" : \"a\", \"teamName\" : \"b\" }", SendRshEmailRequest.class);
        Map<String, String> caseData = new HashMap<>();
        caseData.put("key", "value");

        assertThat(createRshCaseRequest.getSendEmailRequest().getEmail()).isEqualTo(sendEmailRequest.getEmail());
        assertThat(createRshCaseRequest.getSendEmailRequest().getTeamName()).isEqualTo(sendEmailRequest.getTeamName());
        assertThat(createRshCaseRequest.getCaseData()).isEqualTo(caseData);
    }
}
