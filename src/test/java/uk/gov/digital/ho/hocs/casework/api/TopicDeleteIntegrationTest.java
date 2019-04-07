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
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class TopicDeleteIntegrationTest {

    private MockRestServiceServer mockInfoService;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    TopicRepository topicRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID1 = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
    private final UUID CASE_UUID2 = UUID.fromString("24915b78-6977-42db-b343-0915a7f412a1");
    private final UUID STAGE_UUID_ALLOCATED_TO_USER = UUID.fromString("e9151b83-7602-4419-be83-bff1c924c80d");
    private final UUID STAGE_UUID_ALLOCATED_TO_TEAM = UUID.fromString("44d849e4-e7f1-47fb-b4a1-2092270c9b0d");
    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");
    private final UUID TOPIC_UUID = UUID.fromString("d472a1a9-d32d-46cb-a08a-56c22637c584");
    private final UUID TOPIC_UUID2 = UUID.fromString("2a4bd71d-7c8e-4582-8698-8ed689c09075");

    private static final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new GetCaseTypesResponse(new HashSet<>())), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new GetCaseTypesResponse(new HashSet<>())), MediaType.APPLICATION_JSON));
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
    public void shouldReturnOKWhenDeleteTopicForValidCaseWithCaseAllocatedToYou() {

        Topic topicBefore = topicRepository.findByUUID(CASE_UUID1, TOPIC_UUID);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(topicBefore.isDeleted()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(topicRepository.findByUUID(CASE_UUID1, TOPIC_UUID)).isNull();
    }

    @Test
    public void shouldReturnForbiddenWhenDeleteTopicsForValidCaseWithCaseNotAllocatedToYou() {

        long before = topicRepository.findAllByCaseUUID(CASE_UUID2).size();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        long after = topicRepository.findAllByCaseUUID(CASE_UUID2).size();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(after).isEqualTo(before);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteTopicForAnInvalidTopicUUIDCaseAllocateToYou() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + UUID.randomUUID(),
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteTopicForAnInvalidTopicUUIDCaseNoAllocateToYou() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/topic/" + UUID.randomUUID(),
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteTopicForAnInvalidCaseUUID() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteTopicForAnInvalidStageUUID() {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + UUID.randomUUID() + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnOkWhenDeleteATopicForACaseThatIsAllocatedToYouThenReturnForbiddenWhenTheCaseIsAllocatedToAnotherTeam() throws JsonProcessingException {

        setupMockTeams("TEST", 5);

        mockInfoService
                .expect(requestTo("http://localhost:8085/team/44444444-2222-2222-2222-222222222221/contact"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        long before = topicRepository.findAllByCaseUUID(CASE_UUID1).size();

        ResponseEntity<Void> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/team",
                PUT, new HttpEntity(createBodyUpdateTeam(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        long after = topicRepository.findAllByCaseUUID(CASE_UUID1).size();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(after).isEqualTo(before - 1L);
    }

    @Test
    public void shouldReturnForbiddenWhenDeleteATopicForACaseThatIsNotAllocatedToYouThenReturnOkWhenTheCaseIsAllocatedToYou() throws JsonProcessingException {

        setupMockTeams("TEST", 5);

        mockInfoService
                .expect(requestTo("http://localhost:8085/user/4035d37f-9c1d-436e-99de-1607866634d4"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        long before = topicRepository.findAllByCaseUUID(CASE_UUID2).size();
        ResponseEntity<Void> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/topic/" + TOPIC_UUID2,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/user",
                PUT, new HttpEntity(createBodyUpdateUser(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/topic/" + TOPIC_UUID2,
                DELETE, new HttpEntity(createValidAuthHeaders()), Void.class);

        long after = topicRepository.findAllByCaseUUID(CASE_UUID2).size();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(after).isEqualTo(before - 1L);
    }

    @Test
    public void shouldReturnOKWhenDeleteTopicsAndNotFoundWhenDeleteSameTopicForValidCaseWithCaseAllocatedToYou() {

        Topic topicBefore = topicRepository.findByUUID(CASE_UUID1, TOPIC_UUID);

        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(topicBefore.isDeleted()).isFalse();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(topicRepository.findByUUID(CASE_UUID1, TOPIC_UUID)).isNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnOkWhenCaseIsDeletedAndThenNotFoundDeleteTopic() throws JsonProcessingException {

        setupMockTeams("TEST", 5);

        Topic topicBefore = topicRepository.findByUUID(CASE_UUID1, TOPIC_UUID);

        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/topic/" + TOPIC_UUID,
                DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(topicBefore).isNotNull();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "4035d37f-9c1d-436e-99de-1607866634d4");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private void setupMockTeams(String caseType, int permission) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService
                .expect(requestTo("http://localhost:8085/team"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }


    private String createBodyUpdateTeam() {
        return "{\n" +
                " \"teamUUID\" :\"44444444-2222-2222-2222-222222222221\",\n" +
                "  \"allocationType\": \"None\"\n" +
                "}";
    }

    private String createBodyUpdateUser() {
        return "{\n\"userUUID\" :\"4035d37f-9c1d-436e-99de-1607866634d4\"\n}";
    }
}
