package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

    private AuditClient auditClient;
    private static final long caseID = 12345L;
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
    private final UUID caseUUID = randomUUID();
    private LocalDate caseDeadline = LocalDate.now().plusDays(20);
    private LocalDate caseReceived = LocalDate.now();
    private String auditQueue ="audit-queue";
    private Address address = new Address("S1 3NS","some street","some town","some count","UK");
    private Correspondent correspondent = new Correspondent(randomUUID(), "MP", "John Smith", address, "123456789","test@test.com", "1234" );
    private Topic topic = new Topic(caseUUID, "some topic", randomUUID());

    @Captor
    ArgumentCaptor jsonCaptor;
    private String auditService;

    @Before
    public void setUp() {
        when(requestData.correlationId()).thenReturn(randomUUID().toString());
        when(requestData.userId()).thenReturn("some user");
        mapper = configuration.initialiseObjectMapper();
        auditService = "http://audit-service";
        auditClient = new AuditClient(producerTemplate, auditQueue,"hocs-casework","namespace", mapper, requestData, restHelper,
                auditService);
    }

    @Test
    public void shouldSetAuditFields() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.updateCaseAudit(caseData, stageUUID);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
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
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        doThrow(new RuntimeException("An error occurred")).when(producerTemplate).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        assertThatCode(() -> { auditClient.updateCaseAudit(caseData, stageUUID);}).doesNotThrowAnyException();
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
    }

    @Test
    public void createCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.createCaseAudit(caseData);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void updateCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.updateCaseAudit(caseData, stageUUID);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_UPDATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.viewCaseAudit(caseData);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void deleteCaseAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.deleteCaseAudit(caseData);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewCaseSummaryAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        CaseSummary caseSummary = new CaseSummary(LocalDate.now(), new HashMap<>(), new HashSet<>(), correspondent, topic, new HashSet<>());
        auditClient.viewCaseSummaryAudit(caseData, caseSummary);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_SUMMARY_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewStandardLineAudit() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.viewStandardLineAudit(caseData);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STANDARD_LINE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void viewTemplate() throws IOException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(),mapper ,caseDeadline, caseReceived);
        auditClient.viewTemplateAudit(caseData);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.TEMPLATE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void createCorrespondentAudit() throws IOException {
        auditClient.createCorrespondentAudit(correspondent);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CORRESPONDENT_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
    }

    @Test
    public void deleteCorrespondentAudit() throws IOException {
        auditClient.deleteCorrespondentAudit(correspondent);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CORRESPONDENT_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
    }

    @Test
    public void createTopicAudit() throws IOException {
        auditClient.createTopicAudit(caseUUID, topic.getTextUUID());
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_TOPIC_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void deleteTopicAudit() throws IOException {
        auditClient.deleteTopicAudit(caseUUID, topic.getTextUUID());
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_TOPIC_DELETED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void viewCaseNotesAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note");
        auditClient.viewCaseNotesAudit(caseUUID, new HashSet<CaseNote>(){{ add(caseNote); }});
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTES_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
    }

    @Test
    public void viewCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note");
        auditClient.viewCaseNoteAudit(caseNote);
              verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_VIEWED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
    }

    @Test
    public void createCaseNoteAudit() throws IOException {
        CaseNote caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note");
        auditClient.createCaseNoteAudit(caseNote);
              verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.CASE_NOTE_CREATED);
        assertThat(request.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
    }

    @Test
    public void updateStageUser() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(),LocalDate.now(), randomUUID());
        stage.setUser(randomUUID());
        auditClient.updateStageUser(stage);
              verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_ALLOCATED_TO_USER);
        assertThat(request.getCaseUUID()).isEqualTo(stage.getCaseUUID());
    }

    @Test
    public void updateStageTeam() throws IOException {
        Stage stage = new Stage(caseUUID,"SOME_STAGE", randomUUID(),LocalDate.now(), randomUUID());
        stage.setUser(randomUUID());
        auditClient.updateStageTeam(stage);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(auditQueue), jsonCaptor.capture(), any());
        CreateAuditRequest request = mapper.readValue((String)jsonCaptor.getValue(), CreateAuditRequest.class);
        assertThat(request.getType()).isEqualTo(EventType.STAGE_ALLOCATED_TO_TEAM);
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
                "namespace", LocalDateTime.now(),EventType.CASE_CREATED.toString(),
                "user")));

        String events = AuditClient.TIMELINE_EVENTS.stream().collect(Collectors.joining(","));
        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenReturn(restResponse);

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID);
        verify(restHelper, times(1)).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(1);
    }


    @Test
    public void shouldReturnEmptyCaseHistoryWhenAuditServiceCallFails() {
        UUID caseUUID = UUID.randomUUID();
        String events = AuditClient.TIMELINE_EVENTS.stream().collect(Collectors.joining(","));
        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenThrow(new ApplicationExceptions.ResourceException("Error", LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE));

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID);
        verify(restHelper, times(1)).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(0);
    }
}