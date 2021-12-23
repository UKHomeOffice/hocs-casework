package uk.gov.digital.ho.hocs.casework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseDataTypeService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    int port;

    @MockBean()
    @Qualifier("CaseDataService")
    CaseDataService caseDataService;

    @MockBean
    CaseDataTypeService caseDataTypeService;

    @MockBean
    InfoClient infoClient;

    String userId = UUID.randomUUID().toString();
    UUID teamUUID = UUID.fromString("44444444-2222-2222-2222-222222222222");
    CaseDataType caseDataType = CaseDataTypeFactory.from("MIN", "a1");


    @Test
    public void shouldGetCaseDataWhenInCaseTypeGroup() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
        Map<String, String> caseSubData = Map.of("key", "value");

        CaseData caseData = new CaseData(caseDataType, 123456L, caseSubData, LocalDate.now());
        when(caseDataService.getCaseTeams(caseData.getUuid())).thenReturn(Set.of(teamUUID));
        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);
        when(caseDataTypeService.getCaseDataType(caseData.getUuid())).thenReturn(CaseDataTypeFactory.from("MIN", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseData.getUuid(), HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseDataWhenInCaseTypeGroupIfCaseAdmin() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 6));
        Map<String, String> caseSubData = Map.of("key", "value");

        CaseData caseData = new CaseData(caseDataType, 123456L, caseSubData, LocalDate.now());
        when(caseDataService.getCaseTeams(caseData.getUuid())).thenReturn(Set.of(teamUUID));
        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);
        when(caseDataTypeService.getCaseDataType(caseData.getUuid())).thenReturn(CaseDataTypeFactory.from("MIN", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseData.getUuid(), HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseDataWhenNoAccessToCaseTypeInTeamPreviouslyAssignedToCase() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
        Map<String, String> caseSubData = new HashMap<>() {{
            put("key", "value");
        }};

        CaseData caseData = new CaseData(caseDataType, 123456L, caseSubData, LocalDate.now());

        when(caseDataService.getCaseTeams(caseData.getUuid())).thenReturn(Set.of(teamUUID));
        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);
        when(caseDataTypeService.getCaseDataType(caseData.getUuid())).thenReturn(CaseDataTypeFactory.from("TRO", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseData.getUuid(), HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnForbiddenWhenNotInCaseTypeAndNotInTeamPreviouslyAssignedToCase() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.getCaseTeams(caseUUID)).thenReturn(Set.of(UUID.randomUUID()));
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("TRO", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnForbiddenWhenNotInCaseTypeGroup() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
        UUID caseUUID = UUID.randomUUID();

        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("MIN", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/MzMzMzMzMzMzMzMzMzMzMw");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnForbiddenWhenNotInCaseTypeGroupEvenIfCaseAdmin() {

        when(infoClient.getTeams()).thenReturn(setupMockTeams("SOME_CASE_TYPE", 6));
        Map<String, String> caseSubData = new HashMap<>() {{
            put("key", "value");
        }};

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_OTHER_CASE_TYPE", "a1");
        CaseData caseData = new CaseData(caseDataType,123456L, caseSubData, LocalDate.now());

        when(caseDataService.getCaseTeams(caseData.getUuid())).thenReturn(Set.of(UUID.randomUUID()));
        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);
        when(caseDataTypeService.getCaseDataType(caseData.getUuid())).thenReturn(caseDataType);

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseData.getUuid(), HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFound() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.getCase(caseUUID)).thenThrow(new ApplicationExceptions.EntityNotFoundException("Not found", LogEvent.CASE_NOT_FOUND));
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("MIN", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFoundWithAdminTeam() {
        UUID caseUUID = UUID.randomUUID();

        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 6));
        when(caseDataService.getCase(caseUUID)).thenThrow(new ApplicationExceptions.EntityNotFoundException("Not found", LogEvent.CASE_NOT_FOUND));
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("MIN", "01"));

        headers.add(RequestData.USER_ID_HEADER, userId);
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(getBasePath() + "/case/" + caseUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Set<TeamDto> setupMockTeams(String caseType, int permission) {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);
        return teamDtos;
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

}
