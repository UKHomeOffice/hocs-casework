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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class DeleteCorrespondentIntegrationTest {

    private MockRestServiceServer mockInfoService;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    CorrespondentRepository correspondentRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID1 = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
    private final UUID CASE_UUID2 = UUID.fromString("24915b78-6977-42db-b343-0915a7f412a1");
    private final UUID STAGE_UUID_ALLOCATED_TO_USER = UUID.fromString("e9151b83-7602-4419-be83-bff1c924c80d");
    private final UUID STAGE_UUID_ALLOCATED_TO_TEAM = UUID.fromString("44d849e4-e7f1-47fb-b4a1-2092270c9b0d");
    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");
    private final UUID CORRESPONDENT_UUID = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b937");
    private final UUID CORRESPONDENT_UUID2 = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b932");

    private static final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldReturnOKWhenDeleteCorrespondentsForValidCaseWithCaseAllocatedToYou() {

        Correspondent correspondentBefore = correspondentRepository.findByUUID(CASE_UUID1, CORRESPONDENT_UUID);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        assertThat(correspondentBefore.isDeleted()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(correspondentRepository.findByUUID(CASE_UUID1, CORRESPONDENT_UUID)).isNull();
    }

    @Test
    public void shouldReturnForbiddenWhenDeleteCorrespondentsForValidCaseWithCaseNotAllocatedToYou() {

        long before = correspondentRepository.findAllByCaseUUID(CASE_UUID2).size();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        long after = correspondentRepository.findAllByCaseUUID(CASE_UUID2).size();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(after).isEqualTo(before);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteCorrespondentForAnInvalidCorrespondentUUIDCaseAllocateToYou() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + UUID.randomUUID(), DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteCorrespondentForAnInvalidCorrespondentUUIDCaseNoAllocateToYou() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + UUID.randomUUID(), DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteCorrespondentForAnInvalidCaseUUID() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteCorrespondentForAnInvalidStageUUID() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + UUID.randomUUID() + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void shouldReturnOkWhenDeleteACorrespondentForACaseThatIsAllocatedToYouThenReturnForbiddenWhenTheCaseIsAllocatedToAnotherTeam() throws JsonProcessingException {

        mockInfoService
                .expect(requestTo("http://localhost:8085/nominatedpeople/44444444-2222-2222-2222-222222222221"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        long before = correspondentRepository.findAllByCaseUUID(CASE_UUID1).size();

        ResponseEntity<Void> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/team", PUT, new HttpEntity(createBodyUpdateTeam(), createValidAuthHeaders("TEST", "OWNER")), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        long after = correspondentRepository.findAllByCaseUUID(CASE_UUID1).size();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(after).isEqualTo(before - 1l);
    }

    @Test
    public void shouldReturnForbiddenWhenDeleteACorrespondentForACaseThatIsNotAllocatedToYouThenReturnOkWhenTheCaseIsAllocatedToYou() throws JsonProcessingException {

        mockInfoService
                .expect(requestTo("http://localhost:8085/user/4035d37f-9c1d-436e-99de-1607866634d4"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        long before = correspondentRepository.findAllByCaseUUID(CASE_UUID2).size();
        ResponseEntity<Void> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CORRESPONDENT_UUID2, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/user", PUT, new HttpEntity(createBodyUpdateUser(), createValidAuthHeaders("TEST", "OWNER")), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CORRESPONDENT_UUID2, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        long after = correspondentRepository.findAllByCaseUUID(CASE_UUID2).size();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(after).isEqualTo(before - 1l);
    }

    @Test
    public void shouldReturnOKWhenDeleteCorrespondentsAndNotFoundWhenDeleteDameCorrespondentForValidCaseWithCaseAllocatedToYou() {

        Correspondent correspondentBefore = correspondentRepository.findByUUID(CASE_UUID1, CORRESPONDENT_UUID);

        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        assertThat(correspondentBefore.isDeleted()).isFalse();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(correspondentRepository.findByUUID(CASE_UUID1, CORRESPONDENT_UUID)).isNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenCaseIsDeletedAndThenDeleteCorrespondent() throws JsonProcessingException {
        Correspondent correspondentBefore = correspondentRepository.findByUUID(CASE_UUID1, CORRESPONDENT_UUID);

        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);

        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        assertThat(correspondentBefore).isNotNull();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String caseType, String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/UNIT1/44444444-2222-2222-2222-222222222222/" + caseType + "/" + permissionLevel);
        headers.add("X-Auth-Userid", "4035d37f-9c1d-436e-99de-1607866634d4");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }


    private String createBodyUpdateTeam() {
        return "{\n" +
                " \"teamUUID\" :\"44444444-2222-2222-2222-222222222221\",\n" +
                "  \"allocationType\": \"None\"\n" +
                "}";
    }

    private String createBodyUpdateUser() {
        return "{\n" +
                " \"userUUID\" :\"4035d37f-9c1d-436e-99de-1607866634d4\"\n" +
                "}";
    }
}
