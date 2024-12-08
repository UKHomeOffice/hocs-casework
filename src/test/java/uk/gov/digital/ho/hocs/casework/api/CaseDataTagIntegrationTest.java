package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseTagDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseTagRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/tags/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/tags/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles("local")
public class CaseDataTagIntegrationTest {

    private final UUID CASE_UUID = UUID.fromString("fbdbaeab-6719-4e3a-a221-d061dde469a1");

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void shouldAddTagForCase() throws JsonProcessingException {
        var tag = new CaseTagRequest("TEST");
        ResponseEntity<CaseTagDto> result = testRestTemplate.exchange(getBasePath() + "/case/" + CASE_UUID + "/tag",
            POST, new HttpEntity<>(mapper.writeValueAsString(tag), createValidAuthHeaders()), CaseTagDto.class);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCreatedAt())
            .isNotNull()
            .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldReturnSuccessWhenForExistingTag() throws JsonProcessingException {
        var tag = new CaseTagRequest("TEST_TAG");

        ResponseEntity<String> result = testRestTemplate.exchange(getBasePath() + "/case/" + CASE_UUID + "/tag",
            POST, new HttpEntity<>(mapper.writeValueAsString(tag), createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    public void shouldReturnNoContentOnDelete() {
        ResponseEntity<String> result = testRestTemplate.exchange(getBasePath() + "/case/" + CASE_UUID + "/tag/TEST_TAG",
            DELETE, new HttpEntity<>(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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


}
