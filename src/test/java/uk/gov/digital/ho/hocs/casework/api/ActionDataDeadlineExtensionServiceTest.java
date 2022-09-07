package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionInboundDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionDataDeadlineExtensionServiceTest {

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

    public static final Map<String, String> PREV_DATA_CLOB = new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
    }};

    public static final String EXTENSION_REASON_1_SIMPLE_NAME = "EXTENSION_REASON_1_SIMPLE_NAME";

    public static final String EXTENSION_REASON_2_SIMPLE_NAME = "EXTENSION_REASON_2_SIMPLE_NAME";

    public static final EntityDto EXTENSION_REASON_1 = new EntityDto(EXTENSION_REASON_1_SIMPLE_NAME,
        Map.of("title", "Extension Reason 1"));

    public static final EntityDto EXTENSION_REASON_2 = new EntityDto(EXTENSION_REASON_1_SIMPLE_NAME,
        Map.of("title", "Extension Reason 2"));

    public final LocalDate mockedNow = LocalDate.of(2020, 4, 27);

    Set<LocalDate> englandAndWalesBankHolidays2020 = Set.of(LocalDate.parse("2020-01-01"),
        LocalDate.parse("2020-04-10"), LocalDate.parse("2020-04-13"), LocalDate.parse("2020-05-08"),
        LocalDate.parse("2020-05-25"), LocalDate.parse("2020-08-31"), LocalDate.parse("2020-12-25"),
        LocalDate.parse("2020-12-28"));

    private ActionDataDeadlineExtensionService actionDataDeadlineExtensionService;

    @Mock
    private CaseDataRepository mockCaseDataRepository;

    @Mock
    private ActionDataDeadlineExtensionRepository mockExtensionRepository;

    @Mock
    private InfoClient mockInfoClient;

    @Mock
    private AuditClient mockAuditClient;

    @Mock
    private DeadlineService deadlineService;

    @Captor
    private ArgumentCaptor<CaseData> caseDataArgCapture = ArgumentCaptor.forClass(CaseData.class);

    @Captor
    private ArgumentCaptor<ActionDataDeadlineExtension> extensionArgumentCaptor = ArgumentCaptor.forClass(
        ActionDataDeadlineExtension.class);

    @Before
    public void setUp() {
        Clock fixedClock = Clock.fixed(mockedNow.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

        actionDataDeadlineExtensionService = new ActionDataDeadlineExtensionService(mockExtensionRepository,
            mockCaseDataRepository, mockInfoClient, mockAuditClient, deadlineService, fixedClock);

        when(mockInfoClient.getEntityBySimpleName(EXTENSION_REASON_1_SIMPLE_NAME)).thenReturn(EXTENSION_REASON_1);
        when(mockInfoClient.getEntityBySimpleName(EXTENSION_REASON_2_SIMPLE_NAME)).thenReturn(EXTENSION_REASON_2);
    }

    @Test
    public void create_shouldSaveExtension() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 9;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(null,
            actionTypeUuid, "ANY_STRING", "TEST_EXTENSION", "TODAY", extendByDays, "ANY NOTE HERE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        LocalDate originalCaseDeadline = LocalDate.of(2020, Month.APRIL, 30);
        LocalDate originalDeadlineWarning = LocalDate.of(2020, Month.APRIL, 28);

        CaseData previousCaseData = new CaseData(1L, PREVIOUS_CASE_UUID, LocalDateTime.of(2021, Month.APRIL, 1, 0, 0),
            PREVIOUS_CASE_TYPE, PREVIOUS_CASE_REFERENCE, false, PREV_DATA_CLOB, UUID.randomUUID(),
            new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID), UUID.randomUUID(),
            new Correspondent(PREVIOUS_CASE_UUID, PREV_CORRESPONDENT_TYPE, PREV_FULLNAME, PREV_ORGANISATION,
                new Address(PREV_ADDR_1, PREV_ADDR_2, PREV_ADDR_3, PREV_ADDR_4, PREV_ADDR_5), PREV_TELEPHONE,
                PREV_EMAIL, PREV_REFERENCE, PREV_EXTERNAL_KEY), originalCaseDeadline, originalDeadlineWarning,
            mockedNow.minusDays(10), false, Set.of(new ActiveStage(), new ActiveStage()),
            Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        CaseTypeActionDto mockCaseTypeActionDto = new CaseTypeActionDto(actionTypeUuid, null, caseType, null, null,
            "TEST_EXTENSION", 1, 10, true, null);

        when(mockInfoClient.getCaseTypeActionByUuid(previousCaseData.getType(),
            extensionDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(previousCaseData);
        when(mockInfoClient.getCaseType(any())).thenReturn(new CaseDataType(null, null, null, null, 20, 15));
        when(deadlineService.calculateWorkingDaysForCaseType(any(), any(), eq(9))).thenReturn(
            LocalDate.parse("2020-05-11"));

        // WHEN
        actionDataDeadlineExtensionService.createExtension(caseUUID, stageUUID, extensionDto);

        // THEN
        verify(mockExtensionRepository, times(1)).save(extensionArgumentCaptor.capture());

        assertThat(extensionArgumentCaptor.getValue().getUpdatedDeadline()).isEqualTo(LocalDate.parse("2020-05-11"));
        assertThat(extensionArgumentCaptor.getValue().getOriginalDeadline()).isEqualTo(originalCaseDeadline);

        verify(mockCaseDataRepository, times(1)).save(caseDataArgCapture.capture());

        assertThat(caseDataArgCapture.getValue().getCaseDeadline()).isEqualTo(LocalDate.parse("2020-05-11"));
        assertThat(caseDataArgCapture.getValue().getDataMap().get("isCaseExtended").contentEquals("True"));

        verify(mockAuditClient, times(1)).updateCaseAudit(any(), any());

        assertThat(extensionArgumentCaptor.getValue().getNote()).isEqualTo(
            "ANY NOTE HERE\nReason: Extension Reason 1, Extension Reason 2");

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_noActionTypeForIDFound() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(null,
            actionTypeUuid, "ANY_STRING", "TEST_EXTENSION", "TODAY", extendByDays, "ANY NOTE HERE",
            "Reason 1, Reason 2");

        // WHEN
        actionDataDeadlineExtensionService.createExtension(caseUUID, stageUUID, extensionDto);

        // THEN expect throw
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_noCaseForIDFound() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(null,
            actionTypeUuid, "ANY_STRING", "TEST_EXTENSION", "TODAY", extendByDays, "ANY NOTE HERE",
            "Reason 1, Reason 2");

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);
        // WHEN
        actionDataDeadlineExtensionService.createExtension(caseUUID, stageUUID, extensionDto);

        // THEN expect throw
    }

    @Test(expected = HttpClientErrorException.class)
    public void create_shouldAlwaysThrowBadRequestIfSecondWhenMaxConcurrentEventsExceededException() {
        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 9;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(null,
            actionTypeUuid, "TEST_EXTENSION", "ANY_STRING", "TODAY", extendByDays, "ANY NOTE HERE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        ActionDataDeadlineExtension existingExtensionEntity = new ActionDataDeadlineExtension(UUID.randomUUID(), null,
            null, caseUUID, null, null, "ANY NOTE HERE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL, 30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL, 28);

        CaseData previousCaseData = new CaseData(1L, PREVIOUS_CASE_UUID, LocalDateTime.of(2021, Month.APRIL, 1, 0, 0),
            PREVIOUS_CASE_TYPE, PREVIOUS_CASE_REFERENCE, false, PREV_DATA_CLOB, UUID.randomUUID(),
            new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID), UUID.randomUUID(),
            new Correspondent(PREVIOUS_CASE_UUID, PREV_CORRESPONDENT_TYPE, PREV_FULLNAME, PREV_ORGANISATION,
                new Address(PREV_ADDR_1, PREV_ADDR_2, PREV_ADDR_3, PREV_ADDR_4, PREV_ADDR_5), PREV_TELEPHONE,
                PREV_EMAIL, PREV_REFERENCE, PREV_EXTERNAL_KEY), originalCaseDeadline, originalDeadlineWarning,
            mockedNow.minusDays(10), false, Set.of(new ActiveStage(), new ActiveStage()),
            Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        CaseTypeActionDto mockCaseTypeActionDto = new CaseTypeActionDto(actionTypeUuid, null, caseType, null,
            "TEST_EXTENSION", null, 1, 10, true, null);

        when(mockInfoClient.getCaseTypeActionByUuid(previousCaseData.getType(),
            extensionDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(previousCaseData);
        when(mockExtensionRepository.findAllByCaseTypeActionUuidAndCaseDataUuid(actionTypeUuid, caseUUID)).thenReturn(
            List.of(existingExtensionEntity));

        actionDataDeadlineExtensionService.createExtension(caseUUID, stageUUID, extensionDto);
    }

}
