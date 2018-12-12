package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class CaseDataIntegrationTest {

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Test
    public void shouldCreateAValidCase() {

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

    }

    @Test
    public void shouldNotCreateAValidCaseWithIncorrectPermission() {

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("READ")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    //is this right????
    @Test
    public void shouldNotCreateAValidCaseWhenNoRequestBody() {

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(null, createValidAuthHeaders("OWNER")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void shouldNotCreateAValidCaseWithInvalidCaseType() {

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("FRED"), createValidAuthHeaders("OWNER")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    public void shouldNotCreateAValidCaseWithNullCaseType() {

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody(null), createValidAuthHeaders("OWNER")), Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }
    @Test
    public void shouldCreateAValidCaseWithEmptyData() {

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBodyData("MIN","{}"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

    }
    @Test
    public void shouldCreateAValidCaseWithNullData() {

        ResponseEntity<CreateCaseResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBodyData("MIN",null), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getReference()).isNotNull();
        assertThat(result.getBody().getUuid()).isNotNull();

    }

    @Test
    public void shouldCreateTwoValidCasesNumberedSequential() {

        ResponseEntity<CreateCaseResponse> result1 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);
        ResponseEntity<CreateCaseResponse> result2 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result1.getBody().getReference()).isNotNull();
        assertThat(result1.getBody().getUuid()).isNotNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getBody().getReference()).isNotNull();
        assertThat(result2.getBody().getUuid()).isNotNull();

        assertThat(result1.getBody().getReference()).isLessThan(result2.getBody().getReference());
    }

    @Test
    public void shouldCreateValidCaseInvalidCaseValidCaseAndOnlyValidCasesAreNumberedSequential() {

        ResponseEntity<CreateCaseResponse> result1 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);
        ResponseEntity<Void> result2 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("READ")), Void.class);
        ResponseEntity<CreateCaseResponse> result3 = testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(createBody("MIN"), createValidAuthHeaders("OWNER")), CreateCaseResponse.class);

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result1.getBody().getReference()).isNotNull();
        assertThat(result1.getBody().getUuid()).isNotNull();
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getBody().getReference()).isNotNull();
        assertThat(result3.getBody().getUuid()).isNotNull();

        assertThat(result1.getBody().getReference()).isLessThan(result3.getBody().getReference());

        int r1 = Integer.parseInt(result1.getBody().getReference().substring(4, result1.getBody().getReference().length()-3));
        int r3 = Integer.parseInt(result3.getBody().getReference().substring(4, result3.getBody().getReference().length()-3));

        assertThat(r3).isEqualTo(r1+1);
    }


    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/UNIT1/44444444-2222-2222-2222-222222222222/MIN/" + permissionLevel);
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
                "  \"data\": "+data+",\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
        return body;
    }
//    {"DateReceived":"2018-01-01"}

}
