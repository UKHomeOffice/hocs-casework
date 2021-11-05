package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionInboundDto;

import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionDataDeadlineExtensionServiceTest {

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
    private CaseNoteService caseNoteService;

    @Captor
    private ArgumentCaptor<CaseData> caseDataArgCapture = ArgumentCaptor.forClass(CaseData.class);

    @Captor
    private ArgumentCaptor<ActionDataDeadlineExtension> extensionArgumentCaptor = ArgumentCaptor.forClass(ActionDataDeadlineExtension.class);

    private ObjectMapper objectMapper = new ObjectMapper();

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
    public void setUp() {
        actionDataDeadlineExtensionService = new ActionDataDeadlineExtensionService(
                mockExtensionRepository,
                mockCaseDataRepository,
                mockInfoClient,
                mockAuditClient,
                caseNoteService,
                objectMapper
        );
    }

    @Test
    public void create_shouldSaveExtension() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(
                null,
                actionTypeUuid,
                "ANY_STRING",
                "TODAY",
                extendByDays,
                "ANY NOTE HERE"
        );

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        CaseData previousCaseData = new CaseData(
                1l,
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
                null, caseType, null, null,1, 10, true, null
        );

        when(mockInfoClient.getCaseTypeActionByUuid(previousCaseData.getType(), extensionDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(previousCaseData);
        when(mockInfoClient.getCaseDeadline(anyString(), any(LocalDate.class), anyInt())).thenReturn(LocalDate.now().plusDays(extendByDays));
        when(mockInfoClient.getCaseDeadlineWarning(anyString(), any(LocalDate.class), anyInt())).thenReturn(LocalDate.now().plusDays(extendByDays - 2));

       // WHEN
        actionDataDeadlineExtensionService.create(caseUUID,stageUUID, extensionDto);

        // THEN
        verify(mockExtensionRepository, times(1)).save(extensionArgumentCaptor.capture());

        assertThat(extensionArgumentCaptor.getValue().getUpdatedDeadline()).isEqualTo(LocalDate.now().plusDays(8));
        assertThat(extensionArgumentCaptor.getValue().getOriginalDeadline()).isEqualTo(originalCaseDeadline);

        verify(mockCaseDataRepository, times(1)).save(caseDataArgCapture.capture());

        assertThat(caseDataArgCapture.getValue().getCaseDeadline()).isEqualTo(LocalDate.now().plusDays(8));
        assertThat(caseDataArgCapture.getValue().getCaseDeadlineWarning()).isEqualTo(LocalDate.now().plusDays(6));

        verify(caseNoteService, times(1)).createCaseNote(any(), any(), any());

        verify(mockAuditClient, times(1)).updateCaseAudit(any(), any());
        verify(mockAuditClient, times(1)).createExtensionAudit(any());

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_noActionTypeForIDFound() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(
                null,
                actionTypeUuid,
                "ANY_STRING",
                "TODAY",
                extendByDays,
                "ANY NOTE HERE"
        );

        // WHEN
        actionDataDeadlineExtensionService.create(caseUUID,stageUUID, extensionDto);

        // THEN expect throw
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_noCaseForIDFound() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(
                null,
                actionTypeUuid,
                "ANY_STRING",
                "TODAY",
                extendByDays,
                "ANY NOTE HERE"
        );
        CaseTypeActionDto caseTypeActionDto = new CaseTypeActionDto(
                actionTypeUuid, null, caseType, null, null, 1,10, true, null
        );

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);
        // WHEN
        actionDataDeadlineExtensionService.create(caseUUID,stageUUID, extensionDto);

        // THEN expect throw
    }

    @Test(expected = UnsupportedOperationException.class)
    public void update_shouldAlwaysThrowUnsupportedActionException() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(
                actionUuid,
                caseUUID,
                "ANY_STRING",
                "ACTION_LABEL",
                8,
                "ANY NOTE HERE"
        );

        actionDataDeadlineExtensionService.update(caseUUID, stageUUID, actionEntityId, extensionDto);
    }

    @Test(expected = HttpClientErrorException.class)
    public void create_shouldAlwaysThrowBadRequestIfSecondWhenMaxConcurrentEventsExceededException() {
        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 8;
        ActionDataDeadlineExtensionInboundDto extensionDto = new ActionDataDeadlineExtensionInboundDto(
                null,
                actionTypeUuid,
                "ANY_STRING",
                "TODAY",
                extendByDays,
                "ANY NOTE HERE"
        );

        ActionDataDeadlineExtension existingExtensionEntity = new ActionDataDeadlineExtension(
                UUID.randomUUID(),
                null,
                null,
                caseUUID,
                null,
                null,
                null
        );

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        CaseData previousCaseData = new CaseData(
                1l,
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
                null, caseType, null, null,1, 10, true, null
        );

        when(mockInfoClient.getCaseTypeActionByUuid(previousCaseData.getType(), extensionDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(previousCaseData);
        when(mockExtensionRepository.findAllByCaseTypeActionUuidAndCaseDataUuid(actionTypeUuid, caseUUID)).thenReturn(List.of(existingExtensionEntity));

        actionDataDeadlineExtensionService.create(caseUUID, stageUUID, extensionDto);
    }
}