package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataSuspendDto;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataSuspension;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.SuspensionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionDataSuspendServiceTest {

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private SuspensionRepository suspensionRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private CaseNoteService caseNoteService;

    private ActionDataSuspendService actionDataSuspendService;

    private static final UUID NON_EXISTENT_CASE_UUID = UUID.fromString("12cd8a52-4632-4ff1-98a1-6109196c8e35");

    private static final UUID CASE_UUID = UUID.fromString("f51721df-c533-434c-9455-6dc0b3718ae2");

    private static final String TOPIC_NAME = "topic_name";

    private static final UUID TOPIC_NAME_UUID = UUID.fromString("c046fb65-0c3b-4892-b80a-2c239dceded8");

    private static final String CASE_REFERENCE = "TYPE/1234567/21";

    private static final String CASE_TYPE = "TYPE";

    private static final int CASE_TYPE_SLA = 20;

    private static final String CORRESPONDENT_TYPE = "correspondent_type";

    private static final String FULLNAME = "fullname";

    private static final String ORGANISATION = "organisation";

    private static final String ADDR_1 = "addr1";

    private static final String ADDR_2 = "addr2";

    private static final String ADDR_3 = "addr3";

    private static final String ADDR_4 = "add4";

    private static final String ADDR_5 = "addr5";

    private static final String TELEPHONE = "string 1";

    private static final String EMAIL = "string 2";

    private static final String REFERENCE = "string 3";

    private static final String EXTERNAL_KEY = "string 4";

    private static final Map<String, String> DATA_CLOB = new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
    }};

    private static final LocalDate ORIGINAL_CASE_DEADLINE = LocalDate.of(2020, Month.APRIL, 30);

    private static final LocalDate ORIGINAL_CASE_DEADLINE_WARNING = LocalDate.of(2020, Month.APRIL, 28);

    private static final UUID EXISTING_STAGE_UUID = UUID.fromString("1da8c46f-eba0-40d6-bce1-826b0dd35827");

    private static final UUID NON_EXISTENT_ACTION_SUSPEND_TYPE_UUID = UUID.fromString(
        "2f7f4050-8b03-4cc7-8860-e95df90a2cc1");

    private static final UUID ACTION_SUSPEND_TYPE_UUID = UUID.fromString("4d25f909-414e-4aa2-a246-7c9d5df6f07d");

    private static final String ACTION_SUSPEND_SUBTYPE = "SUSPEND";

    private static final String ACTION_SUSPEND_TYPE = "SUSPEND";

    private static final String ACTION_SUSPEND_TYPE_LABEL = "Case Suspension";

    private static final LocalDate DATE_SUSPENSION_SET = LocalDate.of(2021, 6, 1);

    private static final UUID CASE_TYPE_UUID = UUID.fromString("32530e9b-8dec-4f02-992a-c322a7fabdf7");

    private static final int MAX_CONCURRENT_SUSPENSIONS = 1;

    private static final int SORT_ORDER = 10;

    private static final boolean ACTIVE = true;

    private static final String CASE_ACTION_PROPS = "{}";

    private static final CaseTypeActionDto SUSPENSION_CASE_TYPE_ACTION = new CaseTypeActionDto(ACTION_SUSPEND_TYPE_UUID,
        CASE_TYPE_UUID, CASE_TYPE, ACTION_SUSPEND_TYPE, ACTION_SUSPEND_SUBTYPE, ACTION_SUSPEND_TYPE_LABEL,
        MAX_CONCURRENT_SUSPENSIONS, SORT_ORDER, ACTIVE, CASE_ACTION_PROPS);

    private static final ActionDataSuspendDto SUSPEND_REQUEST_DTO = new ActionDataSuspendDto(null,
        ACTION_SUSPEND_TYPE_UUID, ACTION_SUSPEND_SUBTYPE, ACTION_SUSPEND_TYPE_LABEL, DATE_SUSPENSION_SET, null);

    private static final UUID EXISTING_SUSPEND_UUID = UUID.fromString("78c3e33f-7cef-4693-88b1-b0df6fbebfac");

    private static final ActionDataSuspension ACTIVE_SUSPENSION_ENTITY = new ActionDataSuspension(EXISTING_SUSPEND_UUID,
        ACTION_SUSPEND_TYPE_UUID, ACTION_SUSPEND_SUBTYPE, ACTION_SUSPEND_TYPE_LABEL, CASE_TYPE, CASE_UUID,
        DATE_SUSPENSION_SET, null);

    private static CaseData EXISTING_CASE;

    private static final StageTypeDto STAGE_TYPE = new StageTypeDto("Some Stage", "9999", "SOME_STAGE", 20, 18, 1);

    @Before
    public void setUp() {
        actionDataSuspendService = new ActionDataSuspendService(caseDataRepository, suspensionRepository, infoClient,
            auditClient, caseNoteService);

        Correspondent primaryCorrespondent = new Correspondent(CASE_UUID, CORRESPONDENT_TYPE, FULLNAME, ORGANISATION,
            new Address(ADDR_1, ADDR_2, ADDR_3, ADDR_4, ADDR_5), TELEPHONE, EMAIL, REFERENCE, EXTERNAL_KEY);

        ActiveStage stage1 = new ActiveStage();
        ActiveStage stage2 = new ActiveStage();
        stage1.setDeadline(ORIGINAL_CASE_DEADLINE.minusDays(3));
        stage1.setDeadlineWarning(ORIGINAL_CASE_DEADLINE.minusDays(5));
        stage2.setDeadline(ORIGINAL_CASE_DEADLINE.minusDays(2));
        stage2.setDeadlineWarning(ORIGINAL_CASE_DEADLINE.minusDays(4));

        Set<ActiveStage> activeStages = Set.of(stage1, stage2);

        EXISTING_CASE = new CaseData(1L, CASE_UUID, LocalDateTime.of(2021, Month.APRIL, 1, 0, 0), CASE_TYPE,
            CASE_REFERENCE, false, DATA_CLOB, TOPIC_NAME_UUID, new Topic(CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID),
            primaryCorrespondent.getUuid(), primaryCorrespondent, ORIGINAL_CASE_DEADLINE,
            ORIGINAL_CASE_DEADLINE_WARNING, ORIGINAL_CASE_DEADLINE.minusDays(CASE_TYPE_SLA), false, activeStages,
            Set.of(new CaseNote(CASE_UUID, "type", "text", "author")));
    }

    @Test
    public void testSuspendShouldThrowIfCaseNotFound() {
        // GIVEN
        when(caseDataRepository.findActiveByUuid(NON_EXISTENT_CASE_UUID)).thenReturn(null);

        // WHEN - // THEN
        assertThrows("Throw EntityNotFoundException when case found",
            ApplicationExceptions.EntityNotFoundException.class,
            () -> actionDataSuspendService.suspend(NON_EXISTENT_CASE_UUID, EXISTING_STAGE_UUID, SUSPEND_REQUEST_DTO));
    }

    @Test
    public void testSuspendShouldThrowIfActionTypeNotFound() {

        // GIVEN
        ActionDataSuspendDto suspendDto = new ActionDataSuspendDto(null, NON_EXISTENT_ACTION_SUSPEND_TYPE_UUID,
            ACTION_SUSPEND_SUBTYPE, ACTION_SUSPEND_TYPE_LABEL, DATE_SUSPENSION_SET, null);

        when(caseDataRepository.findActiveByUuid(EXISTING_CASE.getUuid())).thenReturn(EXISTING_CASE);
        when(
            infoClient.getCaseTypeActionByUuid(EXISTING_CASE.getType(), suspendDto.getCaseTypeActionUuid())).thenReturn(
            null);

        // WHEN - // THEN
        assertThrows("Throw EntityNotFoundException when case found",
            ApplicationExceptions.EntityNotFoundException.class,
            () -> actionDataSuspendService.suspend(EXISTING_CASE.getUuid(), EXISTING_STAGE_UUID, suspendDto));
    }

    @Test
    public void testSuspendShouldThrowIfHasMaxSuspensionCurrentlyInPlace() {

        // GIVEN
        when(caseDataRepository.findActiveByUuid(EXISTING_CASE.getUuid())).thenReturn(EXISTING_CASE);
        when(infoClient.getCaseTypeActionByUuid(EXISTING_CASE.getType(),
            SUSPEND_REQUEST_DTO.getCaseTypeActionUuid())).thenReturn(SUSPENSION_CASE_TYPE_ACTION);

        when(suspensionRepository.findAllByCaseDataUuidAndCaseTypeActionUuidAndDateSuspensionRemovedIsNull(
            EXISTING_CASE.getUuid(), SUSPENSION_CASE_TYPE_ACTION.getUuid())).thenReturn(
            List.of(ACTIVE_SUSPENSION_ENTITY));

        // WHEN - // THEN
        assertThrows("Throw UnsupportedOperationException if suspension already active.",
            HttpClientErrorException.class,
            () -> actionDataSuspendService.suspend(CASE_UUID, EXISTING_STAGE_UUID, SUSPEND_REQUEST_DTO));
    }

    @Test
    public void testSuspendShouldSuspendCase() {
        // GIVEN
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        when(caseDataRepository.findActiveByUuid(EXISTING_CASE.getUuid())).thenReturn(EXISTING_CASE);
        when(infoClient.getCaseTypeActionByUuid(EXISTING_CASE.getType(),
            SUSPEND_REQUEST_DTO.getCaseTypeActionUuid())).thenReturn(SUSPENSION_CASE_TYPE_ACTION);
        when(infoClient.getAllStagesForCaseType(EXISTING_CASE.getType())).thenReturn(Set.of(STAGE_TYPE));

        // WHEN
        actionDataSuspendService.suspend(EXISTING_CASE.getUuid(), EXISTING_STAGE_UUID, SUSPEND_REQUEST_DTO);

        // THEN

        // CASE DATA CHANGES
        verify(caseDataRepository, times(1)).save(caseDataArgumentCaptor.capture());

        assertEquals("Deadline set to specific date", LocalDate.of(9999, 12, 31),
            caseDataArgumentCaptor.getValue().getCaseDeadline());

        assertEquals("Deadline set to specific date", LocalDate.of(9999, 12, 31),
            caseDataArgumentCaptor.getValue().getCaseDeadlineWarning());

        LocalDate[] stageDeadlines = caseDataArgumentCaptor.getValue().getActiveStages().stream().map(
            ActiveStage::getDeadline).toArray(LocalDate[]::new);
        LocalDate[] expectedStageDeadlines = new LocalDate[stageDeadlines.length];
        Arrays.fill(expectedStageDeadlines, LocalDate.of(9999, 12, 31));

        assertArrayEquals("Deadline set to specific date", expectedStageDeadlines, stageDeadlines);

        assertTrue("That the case data has suspended flag",
            caseDataArgumentCaptor.getValue().getDataMap().containsKey("suspended"));
        assertTrue("That the case data has suspended flag is true",
            Boolean.parseBoolean(caseDataArgumentCaptor.getValue().getData("suspended")));
        assertTrue("That the case data has stage deadline override",
            caseDataArgumentCaptor.getValue().getDataMap().containsKey("SOME_STAGE_DEADLINE"));
        assertEquals("That the case data has stage deadline override with correct date", "9999-12-31",
            caseDataArgumentCaptor.getValue().getDataMap().get("SOME_STAGE_DEADLINE"));
        // Verify other calls.
        verify(auditClient, times(1)).updateCaseAudit(any(CaseData.class), eq(EXISTING_STAGE_UUID));
        verify(auditClient, times(1)).suspendCaseAudit(any(ActionDataSuspension.class));
        verify(suspensionRepository, times(1)).save(any(ActionDataSuspension.class));
        verify(caseNoteService, times(1)).createCaseNote(eq(EXISTING_CASE.getUuid()), eq("CASE_SUSPENSION_APPLIED"),
            eq(""));
    }

    @Test
    public void testUnsuspendShouldThrowIfCaseNotFound() {
        // GIVEN
        UUID existingSuspensionUUID = SUSPEND_REQUEST_DTO.getUuid();
        when(caseDataRepository.findActiveByUuid(NON_EXISTENT_CASE_UUID)).thenReturn(null);

        // WHEN - // THEN
        assertThrows("Throw EntityNotFoundException when case found",
            ApplicationExceptions.EntityNotFoundException.class,
            () -> actionDataSuspendService.unsuspend(NON_EXISTENT_CASE_UUID, EXISTING_STAGE_UUID,
                existingSuspensionUUID));
    }

    @Test
    public void testUnsuspendShouldThrowIfSuspendActionNotFound() {

        // GIVEN
        UUID nonExistingSuspendUUID = UUID.fromString("c7b06e66-62b0-49e4-9d06-93dccc2f1dae");

        when(caseDataRepository.findActiveByUuid(EXISTING_CASE.getUuid())).thenReturn(EXISTING_CASE);

        // WHEN - // THEN
        assertThrows("Throw EntityNotFoundException when case found",
            ApplicationExceptions.EntityNotFoundException.class,
            () -> actionDataSuspendService.unsuspend(EXISTING_CASE.getUuid(), EXISTING_STAGE_UUID,
                nonExistingSuspendUUID));
    }

    @Test
    public void testUnsuspendShouldUnsuspendCase() {
        // GIVEN
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<ActionDataSuspension> suspensionArgumentCaptor = ArgumentCaptor.forClass(
            ActionDataSuspension.class);
        when(caseDataRepository.findActiveByUuid(EXISTING_CASE.getUuid())).thenReturn(EXISTING_CASE);
        when(suspensionRepository.findByUuidAndCaseDataUuid(EXISTING_SUSPEND_UUID, CASE_UUID)).thenReturn(
            Optional.of(ACTIVE_SUSPENSION_ENTITY));
        // WHEN
        actionDataSuspendService.unsuspend(EXISTING_CASE.getUuid(), EXISTING_STAGE_UUID, EXISTING_SUSPEND_UUID);

        // THEN

        // CASE DATA CHANGES
        verify(caseDataRepository, times(1)).save(caseDataArgumentCaptor.capture());

        assertTrue("That the case data has suspended flag",
            caseDataArgumentCaptor.getValue().getDataMap().containsKey("suspended"));
        assertFalse("That the case data has suspended flag is false",
            Boolean.parseBoolean(caseDataArgumentCaptor.getValue().getData("suspended")));

        // Verify other calls.
        verify(auditClient, times(1)).updateCaseAudit(any(CaseData.class), eq(EXISTING_STAGE_UUID));
        verify(auditClient, times(1)).unsuspendCaseAudit(any(ActionDataSuspension.class));
        verify(caseNoteService, times(1)).createCaseNote(eq(EXISTING_CASE.getUuid()), eq("CASE_SUSPENSION_REMOVED"),
            eq(""));
        verify(suspensionRepository, times(1)).save(suspensionArgumentCaptor.capture());

        assertNotNull("That entity saved with updated suspension removed value",
            suspensionArgumentCaptor.getValue().getDateSuspensionRemoved());
    }

}
