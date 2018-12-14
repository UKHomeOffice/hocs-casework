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
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

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
//@Sql(scripts = "classpath:beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class CaseDataIntegrationTest {

    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CaseDataRepository caseDataRepository;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);

    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldCreateAValidCase() {

        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldNotCreateAValidCaseWithIncorrectPermission() {

        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "READ")), Void.class);

        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldNotCreateAValidCaseWithInvalidPermission() {

        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "FRED")), Void.class);

        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldNotCreateAValidCaseWhenNoRequestBody() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(null, createValidAuthHeaders("TEST", "OWNER")), Void.class);

        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldNotCreateAValidCaseWithInvalidCaseType() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("FRED"), createValidAuthHeaders("TEST", "OWNER")), Void.class);

        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldNotCreateAValidCaseWithNullCaseType() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody(null), createValidAuthHeaders("TEST", "OWNER")), Void.class);

        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldCreateAValidCaseWithEmptyData() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBodyData("TEST", "{}"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateAValidCaseWithNullData() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBodyData("TEST", null), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateTwoValidCasesNumberedSequential() {
        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result1 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);
        ResponseEntity<CreateCaseResponse> result2 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseData caseData1 = caseDataRepository.findByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findByUuid(result2.getBody().getUuid());
        Long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result1.getBody().getReference()).isNotNull();
        assertThat(result1.getBody().getUuid()).isNotNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getBody().getReference()).isNotNull();
        assertThat(result2.getBody().getUuid()).isNotNull();
        assertThat(caseData1).isNotNull();
        assertThat(caseData2).isNotNull();

        assertThat(result1.getBody().getReference()).isLessThan(result2.getBody().getReference());
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2l);
    }

    @Test
    public void shouldCreateValidCaseInvalidCaseValidCaseAndOnlyValidCasesAreNumberedSequential() {

        Long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result1 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);
        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "READ")), Void.class);
        ResponseEntity<CreateCaseResponse> result3 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseData caseData1 = caseDataRepository.findByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findByUuid(result3.getBody().getUuid());
        Long numberOfCasesAfter = caseDataRepository.count();

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
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2l);

        int r1 = Integer.parseInt(result1.getBody().getReference().substring(5, result1.getBody().getReference().length() - 3));
        int r3 = Integer.parseInt(result3.getBody().getReference().substring(5, result3.getBody().getReference().length() - 3));

        assertThat(r3).isEqualTo(r1 + 1);
    }

    @Test
    public void shouldGetValidCase() throws JsonProcessingException {

        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));


        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldRequestValidCaseWithEmptyPermission() throws JsonProcessingException {

        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldRequestValidCaseWithInvalidPermission() throws JsonProcessingException {

        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "FRED")), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundForInValidCase() throws JsonProcessingException {
        UUID caseUUID = UUID.randomUUID();
        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseUUID.toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseUUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithEmptyPermission() throws JsonProcessingException {
        UUID caseUUID = UUID.randomUUID();
        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseUUID.toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseUUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithInvalidPermission() throws JsonProcessingException {
        UUID caseUUID = UUID.randomUUID();
        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseUUID.toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseUUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "FRED")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldGetValidCaseWithValidPermissionsAndUnauthorisedWithEmptyPermissionsForSameCase() throws JsonProcessingException {

        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<String> validPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        ResponseEntity<String> invalidPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);

        assertThat(validPermissionResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invalidPermissionResult.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldGetValidCaseWithValidPermissionsAndUnauthorisedWithInvalidPermissionsForSameCase() throws JsonProcessingException {

        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);

        CaseDataType caseDataType = new CaseDataType("TEST", "t1");

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));

        ResponseEntity<String> validPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        ResponseEntity<String> invalidPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), GET, new HttpEntity(createValidAuthHeaders("TEST", "FRED")), String.class);

        assertThat(validPermissionResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invalidPermissionResult.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

//    @Test
//    public void shouldDeleteValidCase() throws JsonProcessingException {
//
//        ResponseEntity<CreateCaseResponse> caseCreated = testRestTemplate.exchange(
//                getBasePath() + "/case", POST, new HttpEntity(createBody("TEST"), createValidAuthHeaders("TEST", "OWNER")), CreateCaseResponse.class);
//
//        CaseDataType caseDataType = new CaseDataType("TEST", "t1");
//
//        mockInfoService
//                .expect(requestTo("http://localhost:8085/caseType/shortCode/" + caseCreated.getBody().getUuid().toString().substring(34)))
//                .andExpect(method(GET))
//                .andRespond(withSuccess(mapper.writeValueAsString(caseDataType), MediaType.APPLICATION_JSON));
//
//        ResponseEntity<String> result = testRestTemplate.exchange(
//                getBasePath() + "/case/" + caseCreated.getBody().getUuid(), DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
//
//        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }


    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String caseType, String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/UNIT1/44444444-2222-2222-2222-222222222222/" + caseType + "/" + permissionLevel);
        headers.add("X-Auth-Userid", "simon.mitchell@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private String createBody(String caseType) {
        String body = "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": {\"DateReceived\":\"2018-01-01\"},\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
        return body;
    }

    private String createBodyData(String caseType, String data) {
        String body = "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": " + data + ",\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
        return body;
    }
}
