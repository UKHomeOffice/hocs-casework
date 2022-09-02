package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.util.SnsStringMessageAttributeValue;
import uk.gov.digital.ho.hocs.casework.utils.BaseAwsTest;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class AuditClientTest extends BaseAwsTest {

    @Captor
    private ArgumentCaptor<PublishRequest> publicRequestCaptor;

    private ResultCaptor<PublishResult> snsPublishResult;

    @SpyBean
    private AmazonSNSAsync auditSearchSnsClient;

    @MockBean(name = "requestData")
    private RequestData requestData;

    @MockBean
    private RestHelper restHelper;

    @Autowired
    private AuditClient auditClient;

    @Value("${hocs.audit-service}")
    private String auditService;


    @Before
    public void setUp() {
        when(requestData.correlationId()).thenReturn(UUID.randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        snsPublishResult = new ResultCaptor<>();
        doAnswer(snsPublishResult).when(auditSearchSnsClient).publish(any());
    }

    @Test
    public void shouldSendCaseCreateEvent() throws JsonProcessingException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        auditClient.createCaseAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.CASE_CREATED);
    }

    @Test
    public void shouldSendCaseUpdateEvent() throws JsonProcessingException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var stageUUID = UUID.randomUUID();
        var caseData = new CaseData(caseType, caseID, new HashMap<>(),  LocalDate.now());

        auditClient.updateCaseAudit(caseData, stageUUID);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.CASE_UPDATED);
    }

    @Test
    public void shouldSendViewCaseEvent() throws JsonProcessingException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        auditClient.viewCaseAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.CASE_VIEWED);
    }

    @Test
    public void shouldSendDeleteCaseEvent() throws JsonProcessingException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        auditClient.deleteCaseAudit(caseData, true);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.CASE_DELETED);
    }

    @Test
    public void viewStandardLineAudit() throws IOException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        auditClient.viewStandardLineAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.STANDARD_LINE_VIEWED);
    }

    @Test
    public void viewTemplate() throws IOException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        auditClient.viewTemplateAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.TEMPLATE_VIEWED);
    }

    @Test
    public void createCorrespondentAudit() throws IOException {
        var address = new Address("TEST", "some street", "some town", "some count", "UK");
        var correspondent = new Correspondent(UUID.randomUUID(), "MP", "John Smith",
                "An Organisation", address, "123456789","test@test.com",
                "1234", "external key" );

        auditClient.createCorrespondentAudit(correspondent);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(correspondent.getCaseUUID(), EventType.CORRESPONDENT_CREATED);
    }

    @Test
    public void deleteCorrespondentAudit() throws IOException {
        var address = new Address("TEST", "some street", "some town", "some count", "UK");
        var correspondent = new Correspondent(UUID.randomUUID(), "MP", "John Smith",
                "An Organisation", address, "123456789","test@test.com",
                "1234", "external key" );

        auditClient.deleteCorrespondentAudit(correspondent);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(correspondent.getCaseUUID(), EventType.CORRESPONDENT_DELETED);
    }

    @Test
    public void updateCorrespondentAudit() throws IOException {
        var address = new Address("TEST", "some street", "some town", "some count", "UK");
        var correspondent = new Correspondent(UUID.randomUUID(), "MP", "John Smith",
                "An Organisation", address, "123456789","test@test.com",
                "1234", "external key" );

        auditClient.updateCorrespondentAudit(correspondent);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(correspondent.getCaseUUID(), EventType.CORRESPONDENT_UPDATED);
    }

    @Test
    public void createTopicAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var topic = new Topic(caseUUID,"topic name", UUID.randomUUID());

        auditClient.createTopicAudit(topic);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(topic.getCaseUUID(), EventType.CASE_TOPIC_CREATED);
    }

    @Test
    public void deleteTopicAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var topic = new Topic(caseUUID,"topic name", UUID.randomUUID());

        auditClient.deleteTopicAudit(topic);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(topic.getCaseUUID(), EventType.CASE_TOPIC_DELETED);
    }

    @Test
    public void viewCaseNotesAudit() throws IOException {
        var caseUUID = UUID.randomUUID();

        auditClient.viewCaseNotesAudit(caseUUID);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseUUID, EventType.CASE_NOTES_VIEWED);
    }

    @Test
    public void viewCaseNoteAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note", "Test User");

        auditClient.viewCaseNoteAudit(caseNote);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseNote.getCaseUUID(), EventType.CASE_NOTE_VIEWED);
    }

    @Test
    public void createCaseNoteAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note", "Test User");

        auditClient.createCaseNoteAudit(caseNote);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseNote.getCaseUUID(), EventType.CASE_NOTE_CREATED,
                Map.of("caseNoteType", "ORIGINAL",
                        "text", "some note"));
    }

    @Test
    public void updateCaseNoteAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var caseNote = new CaseNote(caseUUID, "DRAFT", "post-text", "Test User");

        auditClient.updateCaseNoteAudit(caseNote, "ORIGINAL", "pre-text");

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseNote.getCaseUUID(), EventType.CASE_NOTE_UPDATED,
                Map.of("prevCaseNoteType", "ORIGINAL",
                        "prevText", "pre-text",
                        "caseNoteType", "DRAFT",
                        "text", "post-text"));
    }

    @Test
    public void deleteCaseNoteAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var caseNote = new CaseNote(caseUUID, "ORIGINAL", "some note", "Test User");

        auditClient.deleteCaseNoteAudit(caseNote);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseNote.getCaseUUID(), EventType.CASE_NOTE_DELETED);
    }

    @Test
    public void auditStageUserAllocate() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        auditClient.updateStageUser(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_ALLOCATED_TO_USER);
    }

    @Test
    public void auditStageUserUnallocate() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", UUID.randomUUID(), null, null);

        auditClient.updateStageUser(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_UNALLOCATED_FROM_USER);
    }

    @Test
    public void auditStageTeamAllocate() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        auditClient.updateStageTeam(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_ALLOCATED_TO_TEAM);
    }

    @Test
    public void auditStageTeamUnallocate() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", null, null, null);

        auditClient.updateStageTeam(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_COMPLETED);
    }

    @Test
    public void auditStageCreated() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", null, null, null);

        auditClient.createStage(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_CREATED);
    }



    @Test
    public void shouldRecreateStage() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID,"SOME_STAGE", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        auditClient.recreateStage(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_RECREATED);
    }



    @Test
    public void viewSomuItemsAudit() throws IOException {
        var caseUUID = UUID.randomUUID();

        auditClient.viewAllSomuItemsAudit(caseUUID);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseUUID, EventType.SOMU_ITEMS_VIEWED);
    }

    @Test
    public void viewSomuItemAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var somuItem = new SomuItem(UUID.randomUUID(), caseUUID, UUID.randomUUID(), "{}");

        auditClient.viewCaseSomuItemsBySomuTypeAudit(caseUUID, somuItem.getUuid());

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(somuItem.getCaseUuid(), EventType.SOMU_ITEM_VIEWED);
    }

    @Test
    public void createSomuItemAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var somuItem = new SomuItem(UUID.randomUUID(), caseUUID, UUID.randomUUID(), "{}");

        auditClient.createCaseSomuItemAudit(somuItem);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(somuItem.getCaseUuid(), EventType.SOMU_ITEM_CREATED);
    }

    @Test
    public void updateSomuItemAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var somuItem = new SomuItem(UUID.randomUUID(), caseUUID, UUID.randomUUID(), "{}");

        auditClient.updateSomuItemAudit(somuItem);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(somuItem.getCaseUuid(), EventType.SOMU_ITEM_UPDATED);
    }

    @Test
    public void deleteSomuItemAudit() throws IOException {
        var caseUUID = UUID.randomUUID();
        var somuItem = new SomuItem(UUID.randomUUID(), caseUUID, UUID.randomUUID(), "{}");

        auditClient.deleteSomuItemAudit(somuItem);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(somuItem.getCaseUuid(), EventType.SOMU_ITEM_DELETED);
    }

    @Test
    public void shouldSetHeaders()  {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(),  LocalDate.now());
        Map<String, MessageAttributeValue> expectedHeaders = Map.of(
                "event_type", new SnsStringMessageAttributeValue(EventType.CASE_CREATED.toString()),
                RequestData.CORRELATION_ID_HEADER, new SnsStringMessageAttributeValue(requestData.correlationId()),
                RequestData.USER_ID_HEADER, new SnsStringMessageAttributeValue(requestData.userId()),
                RequestData.USERNAME_HEADER, new SnsStringMessageAttributeValue(requestData.username()),
                RequestData.GROUP_HEADER, new SnsStringMessageAttributeValue(requestData.groups()));

        auditClient.createCaseAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        Assertions.assertTrue(publicRequestCaptor
                .getValue().getMessageAttributes().entrySet()
                .containsAll(expectedHeaders.entrySet()));
    }

    @Test
    public void shouldNotThrowExceptionOnFailure() {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var stageUUID = UUID.randomUUID();
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        doThrow(new RuntimeException("An error occurred")).when(auditSearchSnsClient).publish(any());

        assertThatCode(() -> auditClient.updateCaseAudit(caseData, stageUUID)).doesNotThrowAnyException();
    }

    @Test
    public void shouldGetCaseHistory() {
        var caseUUID = UUID.randomUUID();
        var auditResponseUUID = UUID.randomUUID();
        var restResponse = new GetAuditListResponse(Set.of(new GetAuditResponse(auditResponseUUID,
                caseUUID,
                null,
                "correlation Id",
                "hocs-casework","",
                "namespace", ZonedDateTime.now(),EventType.CASE_CREATED.toString(),
                "user")));
        var events = String.join(",", CaseDataService.TIMELINE_EVENTS);

        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenReturn(restResponse);

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID, CaseDataService.TIMELINE_EVENTS);
        verify(restHelper).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    public void shouldDeleteAuditLinesForCase() {
        var caseUUID = UUID.randomUUID();
        var restResponse = new DeleteCaseAuditResponse("C", caseUUID, false, 1);

        when(restHelper.post(eq(auditService), eq(String.format("/audit/case/%s/delete", caseUUID)),
                any(),
                eq(DeleteCaseAuditResponse.class))).thenReturn(restResponse);

        DeleteCaseAuditResponse response = auditClient.deleteAuditLinesForCase(caseUUID, "C", false);

        verify(restHelper).post(eq(auditService), eq(String.format("/audit/case/%s/delete", caseUUID)),
                any(),
                eq(DeleteCaseAuditResponse.class));
        assertThat(response.getAuditCount()).isEqualTo(1);
    }

    @Test
    public void shouldReturnEmptyCaseHistoryWhenAuditServiceCallFails() {
        var caseUUID = UUID.randomUUID();
        var events = String.join(",", CaseDataService.TIMELINE_EVENTS);

        when(restHelper.get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class)).thenThrow(RestClientException.class);

        Set<GetAuditResponse> response = auditClient.getAuditLinesForCase(caseUUID, CaseDataService.TIMELINE_EVENTS);

        verify(restHelper).get(auditService, String.format("/audit/case/%s?types=%s", caseUUID, events),
                GetAuditListResponse.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testShouldSendCaseActionSuspendCreateAuditMessage() throws JsonProcessingException {
        // GIVEN
        UUID actionUuid = UUID.randomUUID();
        UUID caseTypeActionUuid = UUID.randomUUID();
        String actionSubtype = "SUB_TYPE";
        String caseTypeActionLabel = "Case Suspension";
        String caseDataType = "SMC";
        UUID caseDataUuid = UUID.randomUUID();
        LocalDate dataSuspensionApplied = LocalDate.of(2022, 1, 1);
        LocalDate dataSuspensionRemoved = null;

        ActionDataSuspension suspensionEntity = new ActionDataSuspension(
                actionUuid,
                caseTypeActionUuid,
                actionSubtype,
                caseTypeActionLabel,
                caseDataType,
                caseDataUuid,
                dataSuspensionApplied,
                dataSuspensionRemoved
        );

        // WHEN
        auditClient.suspendCaseAudit(suspensionEntity);

        // THEN
        verify(auditSearchSnsClient, times(1)).publish(publicRequestCaptor.capture());
        assertSnsValues(caseDataUuid, EventType.CASE_SUSPENSION_APPLIED);

    }

    @Test
    public void testShouldSendCaseActionSuspendUnsuspendAuditMessage() throws JsonProcessingException {
        // GIVEN
        UUID actionUuid = UUID.randomUUID();
        UUID caseTypeActionUuid = UUID.randomUUID();
        String actionSubtype = "SUB_TYPE";
        String caseTypeActionLabel = "Case Suspension";
        String caseDataType = "SMC";
        UUID caseDataUuid = UUID.randomUUID();
        LocalDate dataSuspensionApplied = LocalDate.of(2022, 1, 1);
        LocalDate dataSuspensionRemoved = LocalDate.of(2022,2,1);

        ActionDataSuspension suspensionEntity = new ActionDataSuspension(
                actionUuid,
                caseTypeActionUuid,
                actionSubtype,
                caseTypeActionLabel,
                caseDataType,
                caseDataUuid,
                dataSuspensionApplied,
                dataSuspensionRemoved
        );

        // WHEN
        auditClient.unsuspendCaseAudit(suspensionEntity);

        // THEN
        verify(auditSearchSnsClient, times(1)).publish(publicRequestCaptor.capture());
        assertSnsValues(caseDataUuid, EventType.CASE_SUSPENSION_REMOVED);

    }

    private void assertSnsValues(UUID caseUuid, EventType event) throws JsonProcessingException {
        assertSnsValues(caseUuid, event, Collections.emptyMap());
    }

    private void assertSnsValues(UUID caseUuid, EventType event, @NotNull Map<String, String> otherValues) throws JsonProcessingException {
        var caseCreated =
                objectMapper.readValue(publicRequestCaptor.getValue().getMessage(), CreateAuditRequest.class);

        Assertions.assertNotNull(snsPublishResult.getResult());
        Assertions.assertNotNull(snsPublishResult.getResult().getMessageId());
        Assertions.assertEquals(snsPublishResult.getResult().getSdkHttpMetadata().getHttpStatusCode(), 200);
        Assertions.assertEquals(caseCreated.getCaseUUID(), caseUuid);
        Assertions.assertEquals(caseCreated.getType(), event);

        if (!otherValues.isEmpty()) {
            var caseCreatedData =
                    objectMapper.readValue(caseCreated.getAuditPayload(), Map.class);

            Assertions.assertTrue(caseCreatedData.entrySet().containsAll(otherValues.entrySet()));
        }
    }
}
