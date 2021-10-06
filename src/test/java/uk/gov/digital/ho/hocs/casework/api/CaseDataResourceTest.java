package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    public static final UUID RANDOM_UUID = UUID.randomUUID();
    public static final UUID FROM_CASE_UUID = UUID.randomUUID();
    private final CaseDataType caseDataType = CaseDataTypeFactory.from("MIN", "a1");
    private final HashMap<String, String> data = new HashMap<>();
    private final UUID uuid = UUID.randomUUID();
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private CaseNoteService caseNoteService;
    private final LocalDate dateArg = LocalDate.now();

    private CaseDataResource caseDataResource;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService, caseNoteService);
    }

    @Test
    public void shouldCreateCase() {

        //given
        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, dateArg);
        CreateCaseRequest request = new CreateCaseRequest(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID);

        when(caseDataService.createCase(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID)).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseDataType.getDisplayCode(), data, dateArg, FROM_CASE_UUID);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, dateArg);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.of(Boolean.FALSE));

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseNull() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, dateArg);

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
    public void shouldGetCaseSummary() {

        when(caseDataService.getCaseSummary(uuid)).thenReturn(new CaseSummary(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PREVIOUS_CASE_REFERENCE,
                PREVIOUS_CASE_UUID,
                PREVIOUS_STAGE_UUID,
                null));

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
    public void shouldGetCaseWithCorrespondentAndTopic() {

        Correspondent correspondent = new Correspondent(UUID.randomUUID(), "TYPE", "name", "organisation",
                new Address("postcode", "address1", "address2", "address3", "county"),
                "phone", "email", "", "");

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
        doNothing().when(caseDataService).updateDateReceived(uuid, uuid, dateArg, 0);

        // when
        ResponseEntity<Void> response = caseDataResource.updateCaseDateReceived(uuid, uuid, dateArg);

        // then
        verify(caseDataService, times(1)).updateDateReceived(uuid, uuid, dateArg, 0);
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
        String[] texts = {"Text1"};
        UpdateTeamByStageAndTextsRequest request = new UpdateTeamByStageAndTextsRequest(
                uuid, uuid, "stageType", "teamUUIDKey", "teamNameKey", texts);
        Map<String, String> teamMap = new HashMap<>();
        when(caseDataService.updateTeamByStageAndTexts(uuid, uuid, "stageType", "teamUUIDKey", "teamNameKey", texts)).thenReturn(teamMap);

        ResponseEntity<UpdateTeamByStageAndTextsResponse> response = caseDataResource.updateTeamByStageAndTexts(uuid, uuid, request);

        verify(caseDataService).updateTeamByStageAndTexts(uuid, uuid, "stageType", "teamUUIDKey", "teamNameKey", texts);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(UpdateTeamByStageAndTextsResponse.class);
        assertThat(response.getBody().getTeamMap()).isEqualTo(teamMap);
    }

    @Test
    public void shouldGetDocumentTags(){
        UUID caseUUID = UUID.randomUUID();
        List<String> documentTags = List.of("Tag");
        when(caseDataService.getDocumentTags(caseUUID)).thenReturn(documentTags);

        ResponseEntity<List<String>> response = caseDataResource.getDocumentTags(caseUUID);

        assertThat(response.getBody()).isSameAs(documentTags);
        verify(caseDataService).getDocumentTags(caseUUID);
        verifyNoMoreInteractions(caseDataService);
    }

    @Test
    public void shouldEvictFromTheCache() {
        ResponseEntity<String> responseEntity = caseDataResource.clearCachedTemplateForCaseType(caseDataType.getDisplayName());

        assertThat(responseEntity.getBody()).isEqualTo("Cache Cleared");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        UpdatePrimaryCorrespondentRequest primaryCorrespondentUUID =
                new UpdatePrimaryCorrespondentRequest(primaryCorrespondentRequestUUID);

        ResponseEntity<Void> results = caseDataResource.updatePrimaryCorrespondent(
                caseUUID,
                stageUUID,
                primaryCorrespondentUUID
        );

        assertThat(results.getStatusCodeValue()).isEqualTo(200);
        verify(caseDataService).updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentRequestUUID);
        verifyNoMoreInteractions(caseDataService);
    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.of("some_stage_type", 7);

        UpdateDeadlineForStagesRequest updateDeadlineForStagesRequest =
                new UpdateDeadlineForStagesRequest(stageTypeAndDaysMap);


        ResponseEntity<Void> response = caseDataResource.updateDeadlineForStages(uuid, uuid, updateDeadlineForStagesRequest);

        verify(caseDataService).updateDeadlineForStages(uuid, uuid, stageTypeAndDaysMap);
        verifyNoMoreInteractions(caseDataService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getCaseReferenceValue() {
        String reference = "CASE/123456/789";

        when(caseDataService.getCaseDataCaseRef(uuid)).thenReturn(reference);

        final ResponseEntity<GetCaseReferenceResponse> caseReferenceResponse = caseDataResource.getCaseReference(uuid);

        assertThat(caseReferenceResponse).isNotNull();
        assertThat(caseReferenceResponse.getBody()).isNotNull();
        assertThat(caseReferenceResponse.getBody().getReference()).isEqualTo(reference);
        verify(caseDataService).getCaseDataCaseRef(uuid);
        verifyNoMoreInteractions(caseDataService);

    }

    @Test
    public void shouldApplyExtension() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        String testType = "TEST_TYPE";
        String reference = "CASE/123456/789";;
        String reason = "extension reason";

        when(caseDataService.getCaseRef(caseUUID)).thenReturn(reference);

        ApplyExtensionRequest applyExtensionRequest = new ApplyExtensionRequest(testType, reason);
        ResponseEntity<GetCaseReferenceResponse> response = caseDataResource.applyExtension(caseUUID, stageUUID, applyExtensionRequest);

        assertThat(response.getBody().getReference()).isEqualTo(reference);

        verify(caseNoteService).createCaseNote(caseUUID, "EXTENSION", reason);
        verify(caseDataService).applyExtension(caseUUID, stageUUID, applyExtensionRequest.getType(), reason);
        verify(caseDataService).getCaseRef(caseUUID);

        verifyNoMoreInteractions(caseDataService, caseNoteService);
    }
}

