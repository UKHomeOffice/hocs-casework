package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataAppealDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataAppealsRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionDataAppealsServiceTest {

    private ActionDataAppealsService actionDataAppealsService;

    @Mock
    private CaseDataRepository mockCaseDataRepository;

    @Mock
    private InfoClient mockInfoClient;

    @Mock
    private ActionDataAppealsRepository mockAppealRepository;

    @Mock
    private AuditClient mockAuditClient;

    @Mock
    private CaseNoteService mockCaseNoteService;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Captor
    private ArgumentCaptor<ActionDataAppeal> appealArgumentCaptor = ArgumentCaptor.forClass(ActionDataAppeal.class);

    public static final UUID PREVIOUS_CASE_UUID = UUID.randomUUID();
    public static final String TOPIC_NAME = "topic_name";
    public static final UUID TOPIC_NAME_UUID = UUID.randomUUID();
    public static final String PREVIOUS_CASE_REFERENCE = "COMP/1234567/21";
    public static final String PREVIOUS_CASE_TYPE = "COMP";
    public static final String PREV_CORRESPONDENT_TYPE = "correspondent_type";
    public static final String PREV_FULLNAME = "fullname";
    public static final String PREV_ORGANISATION = "organisation";
    public static final String PREV_ADDR_1 = "addr1";
    public static final String PREV_ADDR_2 = "addr2";
    public static final String PREV_ADDR_3 = "addr3";
    public static final String PREV_ADDR_4 = "add4";
    public static final String PREV_ADDR_5 = "addr5";
    public static final String PREV_TELEPHONE = "string 1";
    public static final String PREV_EMAIL = "string 2";
    public static final String PREV_REFERENCE = "string 3";
    public static final String PREV_EXTERNAL_KEY = "string 4";
    public static final String PREV_DATA_CLOB = "{\"key1\" : \"value1\", \"key2\" : \"value2\"}";

    @Before
    public void setUp() throws Exception {

        actionDataAppealsService = new ActionDataAppealsService(
                mockAppealRepository,
                mockCaseDataRepository,
                mockInfoClient,
                mockAuditClient,
                mockCaseNoteService
        );
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_shouldThrowWhenActionNotExist() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                null,
                actionTypeUuid,
                "TEST_APPEAL",
                "ACTION_LABEL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        // WHEN
        actionDataAppealsService.createAppeal(caseUUID, stageUUID, appealDto);

        // THEN Throws

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_shouldThrowWhenCaseNotExist() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                null,
                actionTypeUuid,
                "TEST_APPEAL",
                "ACTION_LABEL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        // WHEN
        actionDataAppealsService.createAppeal(caseUUID, stageUUID, appealDto);

        // THEN Throws
    }

    @Test
    public void create_shouldCreateNewActionForCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                null,
                actionTypeUuid,
                "TEST_APPEAL",
                "ACTION_LABEL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        CaseData caseData = new CaseData(
                1L,
                PREVIOUS_CASE_UUID,
                LocalDateTime.of(2021, Month.APRIL,1, 0,0),
                PREVIOUS_CASE_TYPE,
                PREVIOUS_CASE_REFERENCE,
                false,
                PREV_DATA_CLOB,
                UUID.randomUUID(),
                new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID),
                UUID.randomUUID(),
                new Correspondent(PREVIOUS_CASE_UUID,
                        PREV_CORRESPONDENT_TYPE,
                        PREV_FULLNAME,
                        PREV_ORGANISATION,
                        new Address(PREV_ADDR_1,
                                PREV_ADDR_2,
                                PREV_ADDR_3,
                                PREV_ADDR_4,
                                PREV_ADDR_5),
                        PREV_TELEPHONE,
                        PREV_EMAIL,
                        PREV_REFERENCE,
                        PREV_EXTERNAL_KEY),
                originalCaseDeadline,
                originalDeadlineWarning,
                LocalDate.now().minusDays(10),
                false,
                Set.of(new ActiveStage(), new ActiveStage()),
                Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        CaseTypeActionDto mockCaseTypeActionDto = new CaseTypeActionDto(
                actionTypeUuid,
                null,
                caseType,
                null,
                "TEST_APPEAL",
                appealDto.getCaseTypeActionLabel(),
                1,
                10,
                true,
                null
        );

        ActionDataAppeal createdActionDataAppeal = new ActionDataAppeal();
        createdActionDataAppeal.setUuid(UUID.randomUUID());

        when(mockInfoClient.getCaseTypeActionByUuid(caseData.getType(), appealDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(mockAppealRepository.save((any()))).thenReturn(createdActionDataAppeal);

        // WHEN
        final UUID appealUuid = actionDataAppealsService.createAppeal(caseUUID, stageUUID, appealDto);

        // THEN
        verify(mockAppealRepository, times(1)).save(appealArgumentCaptor.capture());

        assertThat(appealArgumentCaptor.getValue().getCaseTypeActionUuid()).isEqualTo(actionTypeUuid);
        assertThat(appealUuid).isEqualTo(createdActionDataAppeal.getUuid());

        verify(mockCaseDataRepository, times(1)).findActiveByUuid(caseUUID);
        verify(mockCaseNoteService, times(1)).createCaseNote(eq(caseUUID), eq("APPEAL_CREATED"), anyString());
        verify(mockAuditClient, times(1)).createAppealAudit(any(), any());
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void update_shouldThrowWhenActionNotExist() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                actionEntityId,
                actionTypeUuid,
                "ACTION_LABEL",
                "TEST_APPEAL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        // WHEN
        actionDataAppealsService.updateAppeal(caseUUID, actionEntityId, appealDto);

        // THEN Throws
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void update_shouldThrowWhenCaseNotExist() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                actionEntityId,
                actionTypeUuid,
                "ACTION_LABEL",
                "TEST_APPEAL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        // WHEN
        actionDataAppealsService.updateAppeal(caseUUID, actionEntityId, appealDto);

        // THEN Throws
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void update_shouldThrowWhenActionEntityToUpdateNotExist() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                actionEntityId,
                actionTypeUuid,
                "ACTION_LABEL",
                "TEST_APPEAL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                "{}",
                null
        );

        CaseData caseData = new CaseData(
                1L,
                PREVIOUS_CASE_UUID,
                LocalDateTime.of(2021, Month.APRIL,1, 0,0),
                PREVIOUS_CASE_TYPE,
                PREVIOUS_CASE_REFERENCE,
                false,
                PREV_DATA_CLOB,
                UUID.randomUUID(),
                new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID),
                UUID.randomUUID(),
                new Correspondent(PREVIOUS_CASE_UUID,
                        PREV_CORRESPONDENT_TYPE,
                        PREV_FULLNAME,
                        PREV_ORGANISATION,
                        new Address(PREV_ADDR_1,
                                PREV_ADDR_2,
                                PREV_ADDR_3,
                                PREV_ADDR_4,
                                PREV_ADDR_5),
                        PREV_TELEPHONE,
                        PREV_EMAIL,
                        PREV_REFERENCE,
                        PREV_EXTERNAL_KEY),
                originalCaseDeadline,
                originalDeadlineWarning,
                LocalDate.now().minusDays(10),
                false,
                Set.of(new ActiveStage(), new ActiveStage()),
                Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        CaseTypeActionDto mockCaseTypeActionDto = new CaseTypeActionDto(
                actionTypeUuid,
                null,
                caseType,
                null,
                "TEST_APPEAL",
                null,
                1,
                10,
                true,
                null
        );

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        actionDataAppealsService.updateAppeal(caseUUID, actionEntityId, appealDto);

        // THEN Throws
    }

    @Test
    public void update_shouldUpdateExistingActionForCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        String actionTypeLabel = "A LABEL";
        String updatedDataField = "{\"update\": \"true\"}";

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        ActionDataAppealDto appealDto = new ActionDataAppealDto(
                actionEntityId,
                actionTypeUuid,
                "ACTION_LABEL",
                "TEST_APPEAL",
                null,
                LocalDate.MAX,
                null,
                null,
                "TEST NOTE",
                updatedDataField,
                null
        );

        CaseData caseData = new CaseData(
                1L,
                caseUUID,
                LocalDateTime.of(2021, Month.APRIL,1, 0,0),
                PREVIOUS_CASE_TYPE,
                PREVIOUS_CASE_REFERENCE,
                false,
                PREV_DATA_CLOB,
                UUID.randomUUID(),
                new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID),
                UUID.randomUUID(),
                new Correspondent(PREVIOUS_CASE_UUID,
                        PREV_CORRESPONDENT_TYPE,
                        PREV_FULLNAME,
                        PREV_ORGANISATION,
                        new Address(PREV_ADDR_1,
                                PREV_ADDR_2,
                                PREV_ADDR_3,
                                PREV_ADDR_4,
                                PREV_ADDR_5),
                        PREV_TELEPHONE,
                        PREV_EMAIL,
                        PREV_REFERENCE,
                        PREV_EXTERNAL_KEY),
                originalCaseDeadline,
                originalDeadlineWarning,
                LocalDate.now().minusDays(10),
                false,
                Set.of(new ActiveStage(), new ActiveStage()),
                Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        CaseTypeActionDto mockCaseTypeActionDto = new CaseTypeActionDto(
                actionTypeUuid,
                null,
                caseType,
                null,
                "TEST_APPEAL",
                null,
                1,
                10,
                true,
                null
        );

        ActionDataAppeal existingAppealEntity = new ActionDataAppeal(
                actionEntityId,
                actionTypeUuid,
                "TEST_APPEAL",
                actionTypeLabel,
                caseType,
                null,
                null,
                null,
                null,
                null,
                null,
                "{}",
                LocalDateTime.MIN,
                LocalDateTime.MIN,
                null
        );

        ActionDataAppeal updatedAppealEntity = new ActionDataAppeal(
                actionEntityId,
                actionTypeUuid,
                "TEST_APPEAL",
                actionTypeLabel,
                caseType,
                null,
                null,
                null,
                null,
                null,
                null,
                updatedDataField,
                LocalDateTime.MIN,
                LocalDateTime.MIN,
                null
        );



        when(mockInfoClient.getCaseTypeActionByUuid(caseData.getType(), appealDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(mockAppealRepository.findByUuidAndCaseDataUuid(appealDto.getUuid(), caseUUID)).thenReturn(existingAppealEntity);
        when(mockAppealRepository.save(any(ActionDataAppeal.class))).thenReturn(updatedAppealEntity);

        // WHEN
        actionDataAppealsService.updateAppeal(caseUUID, actionEntityId, appealDto);

        // THEN
        verify(mockAppealRepository, times(1)).save(appealArgumentCaptor.capture());

        assertThat(appealArgumentCaptor.getValue().getAppealOfficerData()).isEqualTo(updatedDataField);

        verify(mockInfoClient, times(1)).getCaseTypeActionByUuid(eq(caseData.getType()), eq(actionTypeUuid));
        verify(mockAuditClient, times(1)).updateAppealAudit(any(), any());
        verify(mockCaseNoteService, times(1)).createCaseNote(eq(caseUUID), eq("APPEAL_UPDATED"), eq(actionTypeLabel));

    }
}