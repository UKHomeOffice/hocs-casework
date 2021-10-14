package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:action/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:action/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseActionServiceIntegrationTest {

    private MockRestServiceServer mockInfoService;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    int port;

    @Before
    public void setUp() {
        mockInfoService = buildMockService(restTemplate);
    }

    @Test
    public void shouldCreateDeadlineExtension() throws JsonProcessingException {

        UUID caseId = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
        UUID caseTypeActionUuid = UUID.randomUUID();
        String caseType = "FOI";
        String actionType = "EXTENSION";
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionDto(caseTypeActionUuid, caseTypeActionLabel, "today", 8, "NOTE");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        System.out.println(requestBody);
        ResponseEntity<GetCaseReferenceResponse> response = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseId + "/caseType/" + caseType + "/action/" + actionType,
                POST,
                new HttpEntity(actionDataDto, createValidAuthHeaders()),
                GetCaseReferenceResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldThrowExceptionIfWrongObject() throws JsonProcessingException {

        UUID caseId = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        String caseType = "FOI";
        String actionType = "EXTENSION";
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionDto(actionTypeUuid, caseTypeActionLabel, "today", 8, "NOTE");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        System.out.println(requestBody);
        ResponseEntity<Void> response = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseId + "/caseType/" + caseType + "/action/" + actionType,
                POST,
                new HttpEntity(actionDataDto, createValidAuthHeaders()),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

}