package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseSummaryLink;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateDeadlineForStagesRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdatePrimaryCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateStageDeadlineRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateTeamByStageAndTextsRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateTeamByStageAndTextsResponse;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    private static final long caseID = 12345L;

    public static final String PREVIOUS_CASE_REFERENCE = "COMP/1234567/21";

    public static final UUID PREVIOUS_CASE_UUID = UUID.randomUUID();

    public static final UUID PREVIOUS_STAGE_UUID = UUID.randomUUID();

    public static final UUID FROM_CASE_UUID = UUID.randomUUID();

    private final CaseDataType caseDataType = CaseDataTypeFactory.from("MIN", "a1");

    private final Map<String, String> data = new HashMap<>(0);

    private final UUID uuid = UUID.randomUUID();

    private static final String CASE_REFERENCE = "WCS/87654321/23";

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private InfoClient infoClient;

    private final LocalDate dateArg = LocalDate.now();

    private CaseDataResource caseDataResource;

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService, infoClient);
    }

    @Test
    public void shouldCreateCase() {

        //given
        CaseData caseData = new CaseData(caseDataType, caseID, data, dateArg);
        CreateCaseRequest request = new CreateCaseRequest(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID);

        when(caseDataService.createCase(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID)).thenReturn(
            caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldMigrateCase() {

        MigrateCaseResponse migrateCaseResponse = new MigrateCaseResponse(UUID.randomUUID(), null);
        MigrateCaseRequest request = new MigrateCaseRequest(caseDataType.getDisplayCode());

        when(caseDataService.migrateCase(caseDataType.getDisplayCode(), FROM_CASE_UUID)).thenReturn(
            migrateCaseResponse);

        ResponseEntity<MigrateCaseResponse> response = caseDataResource.migrateCase(FROM_CASE_UUID, request);

        verify(caseDataService, times(1)).migrateCase(caseDataType.getDisplayCode(), FROM_CASE_UUID);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, dateArg);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.of(Boolean.FALSE));

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseNull() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, dateArg);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.empty());

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCase() {

        doNothing().when(caseDataService).deleteCase(uuid, true);

        ResponseEntity<Void> response = caseDataResource.deleteCase(uuid, true);

        verify(caseDataService, times(1)).deleteCase(uuid, true);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCaseByReference() {

        doNothing().when(caseDataService).deleteCase(uuid, true);
        when(caseDataService.getCaseDataByReference(CASE_REFERENCE)).thenReturn(buildStubCaseData(uuid));

        ResponseEntity<Void> response = caseDataResource.deleteCaseByReference(CASE_REFERENCE, true);

        verify(caseDataService, times(1)).getCaseDataByReference(CASE_REFERENCE);
        verify(caseDataService, times(1)).deleteCase(uuid, true);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseSummary() {

        when(caseDataService.getCaseSummary(uuid)).thenReturn(
            new CaseSummary(null, null, null, null, null, null, null, null, PREVIOUS_CASE_REFERENCE, PREVIOUS_CASE_UUID,
                PREVIOUS_STAGE_UUID, null, null));

        ResponseEntity<GetCaseSummaryResponse> response = caseDataResource.getCaseSummary(uuid);

        verify(caseDataService, times(1)).getCaseSummary(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();

        // check the fields are returned
        GetCaseSummaryResponse body = response.getBody();
        assertThat(body.getPreviousCase()).isNotNull();
        CaseSummaryLink link = body.getPreviousCase();
        assertThat(link.getCaseReference()).isEqualTo(PREVIOUS_CASE_REFERENCE);
        assertThat(link.getCaseUUID()).isEqualTo(PREVIOUS_CASE_UUID);
        assertThat(link.getStageUUID()).isEqualTo(PREVIOUS_STAGE_UUID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseTeams() {
        UUID team1UUID = UUID.randomUUID();
        UUID team2UUID = UUID.randomUUID();

        UserDto user1 = new UserDto(
            "1",
            "user 1",
            "user",
            "one",
            "email1"
        );

        UserDto user2 = new UserDto(
            "2",
            "user 2",
            "user",
            "two",
            "email2"
        );

        when(caseDataService.getCaseTeams(uuid)).thenReturn(Set.of(team1UUID, team2UUID));
        when(infoClient.getUsersForTeam(team1UUID)).thenReturn(List.of(user1));
        when(infoClient.getUsersForTeam(team2UUID)).thenReturn(List.of(user2));

        ResponseEntity<List<UserDto>> response = caseDataResource.getCaseTeams(uuid);

        verify(caseDataService, times(1)).getCaseTeams(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsAll(List.of(user1, user2));
    }

    @Test
    public void shouldGetCaseWithCorrespondentAndTopic() {

        Correspondent correspondent = new Correspondent(UUID.randomUUID(), "TYPE", "name", "organisation",
            new Address("postcode", "address1", "address2", "address3", "county"), "phone", "email", "", "");

        Topic topic = new Topic(UUID.randomUUID(), "name", UUID.randomUUID());
        CaseData caseData = mock(CaseData.class);

        when(caseData.getPrimaryCorrespondent()).thenReturn(correspondent);
        when(caseData.getPrimaryTopic()).thenReturn(topic);
        when(caseData.getCreated()).thenReturn(LocalDateTime.of(2019, 1, 1, 6, 0));
        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.of(Boolean.TRUE));

        assertThat(response).isNotNull();
        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPrimaryCorrespondent()).isInstanceOf(GetCorrespondentResponse.class);
        assertThat(response.getBody().getPrimaryTopic()).isInstanceOf(GetTopicResponse.class);
    }

    @Test
    public void shouldCalculateTotals() {
        Map<String, String> totals = new HashMap<>();
        when(caseDataService.calculateTotals(uuid, uuid, "list")).thenReturn(totals);

        ResponseEntity<Map<String, String>> response = caseDataResource.calculateTotals(uuid, uuid, "list");

        verify(caseDataService, times(1)).calculateTotals(uuid, uuid, "list");
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    public void shouldUpdateCaseData() {
        UpdateCaseDataRequest updateCaseDataRequest = new UpdateCaseDataRequest(data);

        doNothing().when(caseDataService).updateCaseData(uuid, uuid, data);

        ResponseEntity<Void> response = caseDataResource.updateCaseData(uuid, uuid, updateCaseDataRequest);

        verify(caseDataService, times(1)).updateCaseData(uuid, uuid, data);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateDateReceived() {

        // given
        doNothing().when(caseDataService).updateDateReceived_defaultSla(uuid, uuid, dateArg);

        // when
        ResponseEntity<Void> response = caseDataResource.updateCaseDateReceived(uuid, uuid, dateArg);

        // then
        verify(caseDataService, times(1)).updateDateReceived_defaultSla(uuid, uuid, dateArg);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateDispatchDeadlineDate() {

        // given
        doNothing().when(caseDataService).updateDispatchDeadlineDate(uuid, uuid, dateArg);

        // when
        ResponseEntity<Void> response = caseDataResource.updateCaseDispatchDeadlineDate(uuid, uuid, dateArg);

        // then
        verify(caseDataService, times(1)).updateDispatchDeadlineDate(uuid, uuid, dateArg);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateStageDeadline() {
        UpdateStageDeadlineRequest updateStageDeadlineRequest = new UpdateStageDeadlineRequest("TEST", 7);
        doNothing().when(caseDataService).updateStageDeadline(uuid, uuid, "TEST", 7);

        ResponseEntity<Void> response = caseDataResource.updateStageDeadline(uuid, uuid, updateStageDeadlineRequest);

        verify(caseDataService).updateStageDeadline(uuid, uuid, "TEST", 7);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateTeamByStageAndTexts() {
        String[] texts = { "Text1" };
        UpdateTeamByStageAndTextsRequest request = new UpdateTeamByStageAndTextsRequest(uuid, uuid, "stageType",
            "teamUUIDKey", "teamNameKey", texts);
        Map<String, String> teamMap = new HashMap<>();
        when(caseDataService.updateTeamByStageAndTexts(uuid, uuid, "stageType", "teamUUIDKey", "teamNameKey",
            texts)).thenReturn(teamMap);

        ResponseEntity<UpdateTeamByStageAndTextsResponse> response = caseDataResource.updateTeamByStageAndTexts(uuid,
            uuid, request);

        verify(caseDataService).updateTeamByStageAndTexts(uuid, uuid, "stageType", "teamUUIDKey", "teamNameKey", texts);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(UpdateTeamByStageAndTextsResponse.class);
        assertThat(response.getBody().getTeamMap()).isEqualTo(teamMap);
    }

    @Test
    public void getCaseDataValue() {
        String variableName = "TestVariableName";
        String expectedValue = "TestValue";

        when(caseDataService.getCaseDataField(uuid, variableName)).thenReturn(expectedValue);

        ResponseEntity<String> results = caseDataResource.getCaseDataValue(uuid, variableName);

        assertThat(results).isNotNull();
        assertThat(results.getBody()).isEqualTo(expectedValue);
        assertThat(results.getStatusCodeValue()).isEqualTo(200);

        verify(caseDataService).getCaseDataField(uuid, variableName);
        verifyNoMoreInteractions(caseDataService);
    }

    @Test
    public void updateCaseDataValue() {
        String variableName = "TestVariableName";

        ResponseEntity<Void> results = caseDataResource.updateCaseDataValue(uuid, variableName, "TestValue");

        assertThat(results).isNotNull();
        assertThat(results.getStatusCodeValue()).isEqualTo(200);

        verify(caseDataService).updateCaseData(uuid, null, Map.of(variableName, "TestValue"));
        verifyNoMoreInteractions(caseDataService);
    }

    @Test
    public void updatePrimaryCorrespondent() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID primaryCorrespondentRequestUUID = UUID.randomUUID();
        UpdatePrimaryCorrespondentRequest primaryCorrespondentUUID = new UpdatePrimaryCorrespondentRequest(
            primaryCorrespondentRequestUUID);

        ResponseEntity<Void> results = caseDataResource.updatePrimaryCorrespondent(caseUUID, stageUUID,
            primaryCorrespondentUUID);

        assertThat(results.getStatusCodeValue()).isEqualTo(200);
        verify(caseDataService).updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentRequestUUID);
        verifyNoMoreInteractions(caseDataService);
    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.of("some_stage_type", 7);

        UpdateDeadlineForStagesRequest updateDeadlineForStagesRequest = new UpdateDeadlineForStagesRequest(
            stageTypeAndDaysMap);

        ResponseEntity<Void> response = caseDataResource.updateDeadlineForStages(uuid, uuid,
            updateDeadlineForStagesRequest);

        verify(caseDataService).updateDeadlineForStages(uuid, uuid, stageTypeAndDaysMap);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testShouldReturnOkWhenMapCaseDataValuesCalled() {

        // GIVEN
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("from1", "to1");
        keyMappings.put("from2", "to2");
        UUID caseUUID = UUID.randomUUID();

        // WHEN
        ResponseEntity<Void> response = caseDataResource.mapCaseDataValues(caseUUID, keyMappings);

        //
        verify(caseDataService).mapCaseDataValues(eq(caseUUID), eq(keyMappings));
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private CaseData buildStubCaseData(UUID uuid) {
        return new CaseData(null, uuid, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null);
    }

}
