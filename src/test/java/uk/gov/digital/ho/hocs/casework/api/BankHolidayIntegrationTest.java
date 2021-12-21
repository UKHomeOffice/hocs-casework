package uk.gov.digital.ho.hocs.casework.api;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
@Sql(scripts = "classpath:bankHoliday/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:bankHoliday/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class BankHolidayIntegrationTest {

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    int port;

    @Before
    public void setUp() throws IOException {
        File firstHolidayResponse = new File(
                this.getClass().getClassLoader().getResource("bank_holidays_response_1.json").getFile()
        );

        File secondHolidayResponse = new File(
                this.getClass().getClassLoader().getResource("bank_holidays_response_2.json").getFile()
        );

        MockRestServiceServer mockBankHolidayApi = buildMockService(restTemplate);

        mockBankHolidayApi
                .expect(requestTo("https://www.gov.uk/bank-holidays.json"))
                .andExpect(method(GET))
                .andRespond(withSuccess(Files.readString(firstHolidayResponse.toPath()), MediaType.APPLICATION_JSON));

        mockBankHolidayApi
                .expect(requestTo("https://www.gov.uk/bank-holidays.json"))
                .andExpect(method(GET))
                .andRespond(withSuccess(Files.readString(secondHolidayResponse.toPath()), MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldUpdateBankHolidaysAndReturn200_thenAddNewBankHolidaysAndReturn200() {

        ResponseEntity<String> firstResponse = testRestTemplate.exchange(
                getBasePath() + "/bankHoliday/refresh",
                GET,
                new HttpEntity<>(createValidAuthHeaders()),
                String.class
        );

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody()).isEqualTo("Success: Added 1 bank holidays");

        ResponseEntity<String> secondResponse = testRestTemplate.exchange(
                getBasePath() + "/bankHoliday/refresh",
                GET,
                new HttpEntity<>(createValidAuthHeaders()),
                String.class
        );

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody()).isEqualTo("Success: Added 3 bank holidays");
    }

    // -------- HELPERS ----------

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
        MockRestServiceServer.MockRestServiceServerBuilder holidayApiBuilder = bindTo(restTemplate);
        holidayApiBuilder.ignoreExpectOrder(true);
        return holidayApiBuilder.build();
    }
}
