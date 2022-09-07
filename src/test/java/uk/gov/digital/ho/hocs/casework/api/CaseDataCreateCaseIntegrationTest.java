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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
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
@ActiveProfiles({ "local", "integration" })
public class CaseDataCreateCaseIntegrationTest {

    ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    int port;

    private MockRestServiceServer mockInfoService;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private CaseDataRepository caseDataRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        mockInfoService = buildMockService(restTemplate);
    }

    @Test
    public void shouldCreateACaseWithPermissionLevelOwner() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();

        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBody("TEST"));

        CaseData caseData = caseDataRepository.findActiveByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1L);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelWrite() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.WRITE.getLevel());

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"));

        long numberOfCasesAfter = caseDataRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelRead() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.READ.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelSummary() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        AccessLevel permissionLevel = AccessLevel.SUMMARY;
        setupMockTeams("TEST", permissionLevel.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelMigrate() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.MIGRATE.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelUnset() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        AccessLevel permissionLevel = AccessLevel.UNSET;
        setupMockTeams("TEST", permissionLevel.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnBadRequestAndNotCreateACaseWhenNoRequestBody() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(null);

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWhenInvalidCaseType() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("WRONG"));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedNotCreateAValidCaseWithNullCaseType() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody(null));

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldCreateAValidCaseWithEmptyData() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBodyData("TEST", "{}"));

        CaseData caseData = caseDataRepository.findActiveByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateAValidCaseWithNullData() throws JsonProcessingException {
        // given
        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());

        // when
        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBodyData("TEST", null));

        // then
        CaseData caseData = caseDataRepository.findActiveByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateTwoValidCasesNumberedSequential() throws JsonProcessingException {
        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<CreateCaseResponse> result1 = getCreateCaseResponse(createBody("TEST"));

        ResponseEntity<CreateCaseResponse> result2 = getCreateCaseResponse(createBody("TEST"));

        CaseData caseData1 = caseDataRepository.findActiveByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findActiveByUuid(result2.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result1.getBody().getReference()).isNotNull();
        assertThat(result1.getBody().getUuid()).isNotNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getBody().getReference()).isNotNull();
        assertThat(result2.getBody().getUuid()).isNotNull();
        assertThat(caseData1).isNotNull();
        assertThat(caseData2).isNotNull();

        assertThat(result1.getBody().getReference()).isLessThan(result2.getBody().getReference());
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2L);
    }

    @Test
    public void shouldCreateValidCaseInvalidCaseValidCaseAndOnlyValidCasesAreNumberedSequential() throws JsonProcessingException {

        long numberOfCasesBefore = caseDataRepository.count();
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        setupMockTeams("TEST", AccessLevel.READ.getLevel());
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<CreateCaseResponse> result1 = getCreateCaseResponse(createBody("TEST"));
        ResponseEntity<Void> result2 = getCreateCaseVoidResponse(createBody("TEST"));
        ResponseEntity<CreateCaseResponse> result3 = getCreateCaseResponse(createBody("TEST"));

        CaseData caseData1 = caseDataRepository.findActiveByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findActiveByUuid(result3.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result1.getBody().getReference()).isNotNull();
        assertThat(result1.getBody().getUuid()).isNotNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getBody().getReference()).isNotNull();
        assertThat(result3.getBody().getUuid()).isNotNull();
        assertThat(result1.getBody().getReference()).isLessThan(result3.getBody().getReference());
        assertThat(caseData1).isNotNull();
        assertThat(caseData2).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2L);

        int r1 = Integer.parseInt(
            result1.getBody().getReference().substring(5, result1.getBody().getReference().length() - 3));
        int r3 = Integer.parseInt(
            result3.getBody().getReference().substring(5, result3.getBody().getReference().length() - 3));

        assertThat(r3).isEqualTo(r1 + 1);
    }

    private ResponseEntity<CreateCaseResponse> getCreateCaseResponse(String body) {
        return testRestTemplate.exchange(getBasePath() + "/case", POST, new HttpEntity(body, createValidAuthHeaders()),
            CreateCaseResponse.class);
    }

    private ResponseEntity<Void> getCreateCaseVoidResponse(String body) {
        return testRestTemplate.exchange(getBasePath() + "/case", POST, new HttpEntity(body, createValidAuthHeaders()),
            Void.class);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private void setupMockTeams(String caseType, int permission) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true,
            permissionDtos);
        teamDtos.add(teamDto);

        Set<String> exemptionDates = Set.of("2020-01-01", "2020-04-10", "2020-04-13", "2020-05-08", "2020-05-25",
            "2020-08-31", "2020-12-25", "2020-12-28", "2021-01-01", "2021-04-02", "2021-04-05", "2021-05-03",
            "2021-05-31", "2021-08-30", "2021-12-27", "2021-12-28", "2022-01-03");

        mockInfoService.expect(requestTo("http://localhost:8085/caseType/type/TEST")).andExpect(method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(CaseDataTypeFactory.from("TEST", "a1")), MediaType.APPLICATION_JSON));

        mockInfoService.expect(requestTo("http://localhost:8085/team")).andExpect(method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    private String createBody(String caseType) {
        return "{\n" + "  \"type\": \"" + caseType + "\",\n" + "  \"data\": {\"DateReceived\":\"2018-01-01\"},\n" + "  \"received\":\"2018-01-01\"\n" + "}";
    }

    private String createBodyData(String caseType, String data) {
        return "{\n" + "  \"type\": \"" + caseType + "\",\n" + "  \"data\": " + data + ",\n" + "  \"received\":\"2018-01-01\"\n" + "}";
    }

}
