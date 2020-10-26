package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    @Mock
    RequestData requestData;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    RestHelper restHelper;

    SpringConfiguration configuration = new SpringConfiguration();
    ObjectMapper mapper;
    UUID stageUUID = UUID.randomUUID();
    String userId = "any user";
    private AuditClient auditClient;
    private static final long caseID = 12345L;
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
    private final UUID caseUUID = randomUUID();
    private LocalDate caseReceived = LocalDate.now();
    private String auditQueue ="audit-queue";
    private Address address = new Address("S1 3NS","some street","some town","some count","UK");
    private Correspondent correspondent = new Correspondent(randomUUID(), "MP", "John Smith", address, "123456789","test@test.com", "1234", "external key" );
    private Topic topic = new Topic(caseUUID, "some topic", randomUUID());

    @Captor
    ArgumentCaptor jsonCaptor;

    @Captor
    ArgumentCaptor<HashMap<String,Object>> headerCaptor;

    private String auditService;

    @Before
    public void setUp() {
        when(requestData.correlationId()).thenReturn(randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        mapper = configuration.initialiseObjectMapper();
        auditService = "http://audit-service";
        auditClient = new AuditClient(producerTemplate, auditQueue,"hocs-casework","namespace", mapper, requestData, restHelper,
                auditService);
        auditClient.setExecutorService(MoreExecutors.newDirectExecutorService());
    }

    @Test
    public void shouldSetDataField() throws IOException {
        UUID topicUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Topic topic = new Topic(caseUUID,"topic name", topicUUID);
        auditClient.createTopicAudit(topic);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getData()).isNotEmpty();
    }

    @Test
    public void shouldSetHeaders()  {
        Map<String, Object> expectedHeaders = Map.of(
        "event_type", EventType.CASE_TOPIC_CREATED.toString(),
        RequestData.CORRELATION_ID_HEADER, requestData.correlationId(),
        RequestData.USER_ID_HEADER, requestData.userId(),
        RequestData.USERNAME_HEADER, requestData.username(),
        RequestData.GROUP_HEADER, requestData.groups());

        UUID topicUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Topic topic = new Topic(caseUUID, "topic name", topicUUID);
        auditClient.createTopicAudit(topic);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), any(), headerCaptor.capture());
        Map headers = headerCaptor.getValue();

        assertThat(headers).containsAllEntriesOf(expectedHeaders);
    }

    @Test
    public void shouldSetAuditFields() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.updateCaseAudit(caseData, stageUUID);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_UPDATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
        assertThat(request.getCorrelationID()).isEqualTo(requestData.correlationId());
        assertThat(request.getNamespace()).isEqualTo("namespace");
        assertThat(request.getRaisingService()).isEqualTo("hocs-casework");
        assertThat(request.getUserID()).isEqualTo(requestData.userId());
    }

    @Test
    public void shouldNotThrowExceptionOnFailure() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        doThrow(new RuntimeException("An error occurred")).when(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        assertThatCode(() -> { auditClient.updateCaseAudit(caseData, stageUUID);}).doesNotThrowAnyException();
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
    }

    @Test
    public void createCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.createCaseAudit(caseData);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void updateCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.updateCaseAudit(caseData, stageUUID);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_UPDATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.viewCaseAudit(caseData);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void deleteCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.deleteCaseAudit(caseData, true);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewCaseSummaryAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.viewCaseSummaryAudit(caseData);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_SUMMARY_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewStandardLineAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.viewStandardLineAudit(caseData);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STANDARD_LINE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewTemplate() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper, caseReceived);
        auditClient.viewTemplateAudit(caseData);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.TEMPLATE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }
    @Test
    public void createCorrespondentAudit() throws IOException {
        auditClient.createCorrespondentAudit(correspondent);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CORRESPONDENT_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
    }

    @Test
    public void deleteCorrespondentAudit() throws IOException {
        auditClient.deleteCorrespondentAudit(correspondent);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CORRESPONDENT_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
    }

    @Test
    public void updateCorrespondentAudit() throws IOException {
        auditClient.updateCorrespondentAudit(correspondent);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CORRESPONDENT_UPDATED);
        assertThat(request.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
    }

    @Test
    public void createTopicAudit() throws IOException {
        Topic topic = new Topic(caseUUID,"topic name", UUID.randomUUID());
        auditClient.createTopicAudit(topic);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_TOPIC_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void deleteTopicAudit() throws IOException {
        auditClient.deleteTopicAudit(topic);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_TOPIC_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void viewCaseNotesAudit() throws IOException {
        auditClient.viewCaseNotesAudit(caseUUID);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTES_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void viewCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note", userId);
        auditClient.viewCaseNoteAudit(caseNote);
              verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
    }

    @Test
    public void createCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note",userId);
        auditClient.createCaseNoteAudit(caseNote);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
        assertThat(request.getAuditPayload()).contains("\"caseNoteType\" : \"ORIGINAL\"");
        assertThat(request.getAuditPayload()).contains("\"text\" : \"some note\"");
    }

    @Test
    public void updateCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "DRAFT", "post-text",userId);
        auditClient.updateCaseNoteAudit(caseNote, "ORIGINAL", "pre-text");
              verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_UPDATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
        assertThat(request.getAuditPayload()).contains("\"prevCaseNoteType\" : \"ORIGINAL\"");
        assertThat(request.getAuditPayload()).contains("\"prevText\" : \"pre-text\"");
        assertThat(request.getAuditPayload()).contains("\"caseNoteType\" : \"DRAFT\"");
        assertThat(request.getAuditPayload()).contains("\"text\" : \"post-text\"");
    }

    @Test
    public void deleteCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note",userId);
        auditClient.deleteCaseNoteAudit(caseNote);
              verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
    }

    @Test
    public void auditStageUserAllocate() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(), randomUUID(), randomUUID());
        stage.setUser(randomUUID());
        auditClient.updateStageUser(stage);
              verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_ALLOCATED_TO_USER);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void auditStageUserUnallocate() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(), null, null);
        stage.setUser(null);
        auditClient.updateStageUser(stage);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_UNALLOCATED_FROM_USER);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void auditStageTeamAllocate() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(), randomUUID(), randomUUID());
        stage.setUser(randomUUID());
        auditClient.updateStageTeam(stage);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_ALLOCATED_TO_TEAM);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void auditStageTeamUnallocate() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", null, null, null);
        auditClient.updateStageTeam(stage);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_COMPLETED);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void auditStageCreated() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", null, null, null);
        auditClient.createStage(stage);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void shouldGetCaseHistory() {
        UUID caseUUID = UUID.randomUUID();
        UUID auditResponseUUID = UUID.randomUUID();
        GetAuditListResponse restResponse = new GetAuditListResponse(Set.of(new GetAuditResponse(auditResponseUUID,
                caseUUID,
                null,
                "correlation Id",
                "hocs-casework","",
                "namespace", ZonedDateTime.now(),EventType.CASE_CREATED.toString(),
                "user")));

        String events = CaseDataService.TIMELINE_EVENTS.stream().collect(Collectors.joining(","));
        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenReturn(restResponse);

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID, CaseDataService.TIMELINE_EVENTS);
        verify(restHelper).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(1);
    }


    @Test
    public void shouldReturnEmptyCaseHistoryWhenAuditServiceCallFails() {
        UUID caseUUID = UUID.randomUUID();
        String events = CaseDataService.TIMELINE_EVENTS.stream().collect(Collectors.joining(","));
        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenThrow(RestClientException.class);

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID, CaseDataService.TIMELINE_EVENTS);
        verify(restHelper).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void shouldRecreateStage() throws IOException {

        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(), randomUUID(), randomUUID());
        auditClient.recreateStage(stage);
        verify(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_RECREATED);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(request.getStageUUID()).isEqualTo(stage.getUuid());
    }

    @Test
    public void shouldDeleteAuditLinesForCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID auditResponseUUID = UUID.randomUUID();
        DeleteCaseAuditResponse restResponse = new DeleteCaseAuditResponse("C", caseUUID, false, 1);
        when(restHelper.post(eq(auditService), eq(String.format("/audit/case/%s/delete", caseUUID)),
                any(),
                eq(DeleteCaseAuditResponse.class))).thenReturn(restResponse);

        DeleteCaseAuditResponse response = auditClient.deleteAuditLinesForCase(caseUUID, "C", false);

        verify(restHelper).post(eq(auditService), eq(String.format("/audit/case/%s/delete", caseUUID)),
                any(),
                eq(DeleteCaseAuditResponse.class));
        assertThat(response.getAuditCount()).isEqualTo(1);
    }

}