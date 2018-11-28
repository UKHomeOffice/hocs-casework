package uk.gov.digital.ho.hocs.casework.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Profile("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    int port;

    @MockBean
    CaseDataService caseDataService;

    String userId = UUID.randomUUID().toString();

    @Autowired
    ObjectMapper mapper;

    @Test
    public void shouldGetCaseDataWhenInCaseTypeGroup() {
        UUID caseUUID = UUID.randomUUID();

        Map<String,String> caseSubData = new HashMap<String, String>(){{
            put("key","value");
        }};

        CaseData caseData = new CaseData(CaseDataType.MIN,123456L, caseSubData, mapper, LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/DCU/team3/MIN/WRITE," +
                                                          "/DCU/team3/MIN/READ");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedWhenNotInCaseTypeGroup() {
        UUID caseUUID = UUID.randomUUID();


        Map<String,String> caseSubData = new HashMap<String, String>(){{
            put("key","value");
        }};

        CaseData caseData = new CaseData(CaseDataType.MIN,123456L, caseSubData, new ObjectMapper(), LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/DCU/team3/TRO/WRITE");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFound() {
        UUID caseUUID = UUID.randomUUID();
        when(caseDataService.getCase(caseUUID)).thenThrow(new EntityNotFoundException("Not found"));
        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/DCU/team3/TRO/WRITE");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    private String getBasePath() {
        return "http://localhost:"+ port;
    }

}
