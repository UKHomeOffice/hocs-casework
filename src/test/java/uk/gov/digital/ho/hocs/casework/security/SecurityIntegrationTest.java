package uk.gov.digital.ho.hocs.casework.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

    @MockBean
    InfoClient infoClient;

    String userId = UUID.randomUUID().toString();
    CaseDataType caseDataType = new CaseDataType("MIN", "a1");

    @Autowired
    ObjectMapper mapper;

    @Before
    public void setup() throws JsonProcessingException {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
    }


    @Test
    public void shouldGetCaseDataWhenInCaseTypeGroup() {
        UUID caseUUID = UUID.randomUUID();

        Map<String,String> caseSubData = new HashMap<String, String>(){{
            put("key","value");
        }};


        CaseData caseData = new CaseData(caseDataType, 123456L, caseSubData, mapper, LocalDate.now(), LocalDate.now());

        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);
        when(caseDataService.getCaseType(caseUUID)).thenReturn("MIN");

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnForbiddenWhenNotInCaseTypeGroup() {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.getCaseType(caseUUID)).thenReturn("MIN");

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/MzMzMzMzMzMzMzMzMzMzMw");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFound() {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.getCase(caseUUID)).thenThrow(new ApplicationExceptions.EntityNotFoundException("Not found", LogEvent.CASE_NOT_FOUND));
        when(caseDataService.getCaseType(caseUUID)).thenReturn("MIN");

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Set<TeamDto>  setupMockTeams(String caseType, int permission) {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);
        return teamDtos;
    }

    private String getBasePath() {
        return "http://localhost:"+ port;
    }

}
