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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles("local")
public class UpdateCorrespondentIntegrationTest {

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

    private final UUID CASE1_EXISTING_CORRESPONDENT_UUID = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b937");

    private final UUID CASE2_EXISTING_CORRESPONDENT_UUID = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b932");

    private static final CaseDataType CASE_DATA_TYPE = CaseDataTypeFactory.from("TEST", "a1");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService.expect(requestTo("http://localhost:8085/caseType")).andExpect(method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(ExpectedCount.times(3),
            requestTo("http://localhost:8085/caseType/shortCode/a1")).andExpect(method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/correspondentType/TEST")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(GetCorrespondentTypeResponse.from(Collections.emptySet())),
                MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldReturnOKWhenUpdateACorrespondentForACaseThatIsAllocatedToYou() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(after.getFullName()).isEqualTo("Bob Bloggs");
    }

    @Test
    public void shouldReturnForbiddenWhenUpdateACorrespondentForACaseThatIsNotAllocatedToYou() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(after).isEqualToComparingFieldByField(before);
    }

    public void shouldUpdateACorrespondentForACaseThatIsNotAllocatedToYouButUserCaseAdmin() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.CASE_ADMIN.getLevel(), 2);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(after).isNotSameAs(before);
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateACorrespondentForACaseYouAreAssignedTorWithNullBody() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(null, createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(after).isEqualToComparingFieldByField(before);
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateACorrespondentForACaseThatIsNotAssignedToYouButNoRequestBody() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(null, createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID2, CASE2_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(after).isEqualToComparingFieldByField(before);
    }

    @Test
    public void shouldReturnNotFoundWhenUpdateACorrespondentForAnInvalidCaseUUID() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + INVALID_CASE_UUID + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenUpdateACorrespondentForAnInvalidStageUUID() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + UUID.randomUUID() + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenUpdateACorrespondentForAnInvalidCaseUUIDAndAnInvalidStageUUID() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + INVALID_CASE_UUID + "/stage/" + UUID.randomUUID() + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateACorrespondentForACaseThatIsAllocatedToYouWithNullFullName() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBodyNullFullName(), createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(after).isEqualToComparingFieldByField(before);
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateACorrespondentForACaseThatIsAllocatedToYouWithEmptyFullName() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        Correspondent before = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        ResponseEntity<Void> result = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBodyEmptyFullName(), createValidAuthHeaders()), Void.class);

        Correspondent after = correspondentRepository.findByUUID(CASE_UUID1, CASE1_EXISTING_CORRESPONDENT_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(after).isEqualToComparingFieldByField(before);
    }

    @Test
    public void shouldReturnOkWhenUpdateACorrespondentForACaseThatIsAllocatedToYouThenReturnForbiddenWhenTheCaseIsAllocatedToAnotherTeam() throws JsonProcessingException {

        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 5);

        mockInfoService.expect(
            requestTo("http://localhost:8085/team/44444444-2222-2222-2222-222222222221/contact")).andExpect(
            method(GET)).andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Void> result1 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/team", PUT,
            new HttpEntity(createBodyUpdateTeam(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CASE1_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    public void shouldReturnForbiddenWhenCreateACorrespondentForACaseThatIsNotAllocatedToYouThenReturnOkWhenTheCaseIsAllocatedToYou() throws JsonProcessingException {

        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 5);

        mockInfoService.expect(requestTo("http://localhost:8085/user/4035d37f-9c1d-436e-99de-1607866634d4")).andExpect(
            method(GET)).andRespond(withSuccess("{\"emailAddress\":\"bob\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Void> result1 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result2 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/user", PUT,
            new HttpEntity(createBodyUpdateUser(), createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> result3 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_UUID2 + "/stage/" + STAGE_UUID_ALLOCATED_TO_TEAM + "/correspondent/" + CASE2_EXISTING_CORRESPONDENT_UUID,
            PUT, new HttpEntity(createBody(), createValidAuthHeaders()), Void.class);

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    private void setupMockTeams(String caseType, int permission, int noOfCalls) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true,
            permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService.expect(ExpectedCount.times(noOfCalls), requestTo("http://localhost:8085/team")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }

    private String createBody() {
        return "{\n" + "  \"type\": \"CORRESPONDENT\",\n" + "  \"fullname\": \"Bob Bloggs\",\n" + "  \"postcode\":\"S1 1DJ\",\n" + "  \"address1\":\"1 Somewhere Street\",\n" + "  \"address2\":\"Sheffield\",\n" + "  \"address3\":\"\",\n" + "  \"country\":\"England\",\n" + "  \"telephone\":\"0115 2595959\",\n" + "  \"email\":\"a@a.com\",\n" + "  \"reference\":\"\"\n" + "}";
    }

    private String createBodyNullCorrespondentType() {
        return "{\n" + "  \"type\": null,\n" + "  \"fullname\": \"Bob Bloggs\",\n" + "  \"postcode\":\"S1 1DJ\",\n" + "  \"address1\":\"1 Somewhere Street\",\n" + "  \"address2\":\"Sheffield\",\n" + "  \"address3\":\"\",\n" + "  \"country\":\"England\",\n" + "  \"telephone\":\"0115 2595959\",\n" + "  \"email\":\"a@a.com\",\n" + "  \"reference\":\"\"\n" + "}";
    }

    private String createBodyNullFullName() {
        return "{\n" + "  \"type\": \"MEMBER\",\n" + "  \"fullname\": null,\n" + "  \"postcode\":\"S1 1DJ\",\n" + "  \"address1\":\"1 Somewhere Street\",\n" + "  \"address2\":\"Sheffield\",\n" + "  \"address3\":\"\",\n" + "  \"country\":\"England\",\n" + "  \"telephone\":\"0115 2595959\",\n" + "  \"email\":\"a@a.com\",\n" + "  \"reference\":\"\"\n" + "}";
    }

    private String createBodyEmptyFullName() {
        return "{\n" + "  \"type\": \"MEMBER\",\n" + "  \"fullname\": \"\",\n" + "  \"postcode\":\"S1 1DJ\",\n" + "  \"address1\":\"1 Somewhere Street\",\n" + "  \"address2\":\"Sheffield\",\n" + "  \"address3\":\"\",\n" + "  \"country\":\"England\",\n" + "  \"telephone\":\"0115 2595959\",\n" + "  \"email\":\"a@a.com\",\n" + "  \"reference\":\"\"\n" + "}";
    }

    private String createBodyUpdateTeam() {
        return "{\n" + " \"teamUUID\" :\"44444444-2222-2222-2222-222222222221\",\n" + "  \"allocationType\": \"None\"\n" + "}";
    }

    private String createBodyUpdateUser() {
        return "{\n" + " \"userUUID\" :\"4035d37f-9c1d-436e-99de-1607866634d4\"\n" + "}";
    }

}
