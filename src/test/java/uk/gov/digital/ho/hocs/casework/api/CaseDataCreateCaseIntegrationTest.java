package uk.gov.digital.ho.hocs.casework.api;

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
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Sql(scripts = "classpath:beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class CaseDataCreateCaseIntegrationTest {


    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private CaseDataRepository caseDataRepository;


    @Test
    public void shouldCreateACaseWithPermissionLevelOwner() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBody("TEST"), "TEST", "OWNER");

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateACaseWithPermissionLevelWrite() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBody("TEST"), "TEST", "WRITE");

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelRead() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "READ");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelSummary() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "SUMMARY");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelUnset() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "UNSET");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithInvalidPermission() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "WRONG");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithEmptyPermission() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelNull() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("TEST"), "TEST", null);

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnBadRequestAndNotCreateACaseWhenNoRequestBody() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(null, "TEST", "OWNER");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWhenInvalidCaseType() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody("WRONG"), "TEST", "OWNER");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedNotCreateAValidCaseWithNullCaseType() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<Void> result = getCreateCaseVoidResponse(createBody(null), "TEST", "OWNER");

        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldCreateAValidCaseWithEmptyData() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBodyData("TEST","{}"), "TEST", "OWNER");

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateAValidCaseWithNullData() {
        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result = getCreateCaseResponse(createBodyData("TEST",null), "TEST", "OWNER");

        CaseData caseData = caseDataRepository.findByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseDataRepository.count();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    @Test
    public void shouldCreateTwoValidCasesNumberedSequential() {
        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result1 = getCreateCaseResponse(createBody("TEST"), "TEST", "OWNER");
        ResponseEntity<CreateCaseResponse> result2 = getCreateCaseResponse(createBody("TEST"), "TEST", "OWNER");

        CaseData caseData1 = caseDataRepository.findByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findByUuid(result2.getBody().getUuid());
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
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2l);
    }

    @Test
    public void shouldCreateValidCaseInvalidCaseValidCaseAndOnlyValidCasesAreNumberedSequential() {

        long numberOfCasesBefore = caseDataRepository.count();

        ResponseEntity<CreateCaseResponse> result1 = getCreateCaseResponse(createBody("TEST"), "TEST", "OWNER");
        ResponseEntity<Void> result2 = getCreateCaseVoidResponse(createBody("TEST"), "TEST", "READ");
        ResponseEntity<CreateCaseResponse> result3 = getCreateCaseResponse(createBody("TEST"), "TEST", "OWNER");

        CaseData caseData1 = caseDataRepository.findByUuid(result1.getBody().getUuid());
        CaseData caseData2 = caseDataRepository.findByUuid(result3.getBody().getUuid());
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
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 2l);

        int r1 = Integer.parseInt(result1.getBody().getReference().substring(5, result1.getBody().getReference().length() - 3));
        int r3 = Integer.parseInt(result3.getBody().getReference().substring(5, result3.getBody().getReference().length() - 3));

        assertThat(r3).isEqualTo(r1 + 1);
    }


    private ResponseEntity<CreateCaseResponse> getCreateCaseResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(body, createValidAuthHeaders(caseTypePermission, permissionLevel)), CreateCaseResponse.class);
    }


    private ResponseEntity<Void> getCreateCaseVoidResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(body, createValidAuthHeaders(caseTypePermission, permissionLevel)), Void.class);
    }


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
        return "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": {\"DateReceived\":\"2018-01-01\"},\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
    }

    private String createBodyData(String caseType, String data) {
        return "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": " + data + ",\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
    }
}