package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataExternalInterestInboundDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataExternalInterestRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActionDataExternalInterestServiceTest {

    private ActionDataExternalInterestService actionDataExternalInterestService;

    @Mock
    private CaseDataRepository mockCaseDataRepository;

    @Mock
    private ActionDataExternalInterestRepository mockExternalInterestRepository;

    @Mock
    private InfoClient mockInfoClient;

    @Mock
    private AuditClient mockAuditClient;

    @Mock
    private CaseNoteService mockCaseNoteService;

    @Captor
    private ArgumentCaptor<ActionDataExternalInterest> externalInterestArgumentCaptor
            = ArgumentCaptor.forClass(ActionDataExternalInterest.class);

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
        actionDataExternalInterestService = new ActionDataExternalInterestService(
                mockExternalInterestRepository,
                mockCaseDataRepository,
                mockInfoClient,
                mockAuditClient,
                mockCaseNoteService
        );
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void create_noCaseForIDFound() {

        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        int extendByDays = 8;
        ActionDataExternalInterestInboundDto extensionDto = new ActionDataExternalInterestInboundDto(
                null,
                actionTypeUuid,
                "ANY_STRING",
                "ANY_PARTY",
                "ANY NOTE HERE"
        );
        CaseTypeActionDto caseTypeActionDto = new CaseTypeActionDto(
                actionTypeUuid, null, caseType, null, null, 1,10, true, null
        );

        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);
        // WHEN
        actionDataExternalInterestService.create(caseUUID,stageUUID, extensionDto);

        // THEN expect throw specified in annotation
    }

    @Test
    public void create_shouldCreateNewActionForCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        ActionDataExternalInterestInboundDto externalInterestDto = new ActionDataExternalInterestInboundDto(
                null,
                actionTypeUuid,
                "External Interest",
                "TEST_PARTY_TYPE",
                "TEST_DETAILS"
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
                null, caseType, null, externalInterestDto.getCaseTypeActionLabel(), 1,10, true, null
        );

        when(mockInfoClient.getCaseTypeActionByUuid(caseData.getType(),
                externalInterestDto.getCaseTypeActionUuid())).thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        actionDataExternalInterestService.create(caseUUID, stageUUID, externalInterestDto);

        // THEN
        verify(mockExternalInterestRepository, times(1)).save(externalInterestArgumentCaptor.capture());

        assertThat(externalInterestArgumentCaptor.getValue().getCaseTypeActionUuid()).isEqualTo(actionTypeUuid);

        verify(mockCaseDataRepository, times(1)).findActiveByUuid(caseUUID);
        verify(mockCaseNoteService, times(1)).createCaseNote(eq(caseUUID), eq("RECORD_INTEREST"), anyString());
        verify(mockAuditClient, times(1)).createExternalInterestAudit(any());
    }

    @Test
    public void update_shouldUpdateExistingActionForCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID actionTypeUuid = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "TEST_CASE_TYPE";
        String actionTypeLabel = "TEST_DETAILS_CHANGED";

        LocalDate originalCaseDeadline = LocalDate.of(2021, Month.APRIL,30);
        LocalDate originalDeadlineWarning = LocalDate.of(2021, Month.APRIL,28);

        ActionDataExternalInterestInboundDto actionDataExternalInterestDto = new ActionDataExternalInterestInboundDto(
                actionEntityId,
                actionTypeUuid,
                "ACTION_LABEL",
                "TEST_PARTY_TYPE_CHANGED",
                "TEST_DETAILS_CHANGED"
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
                null, caseType, null, null, 1,10, true, null
        );

        ActionDataExternalInterest existingExternalInterestEntity = new ActionDataExternalInterest(
                actionTypeUuid,
                actionTypeLabel,
                caseType,
                null,
                "TEST_PARTY_TYPE",
                "TEST_DETAILS"
        );

        ActionDataExternalInterest updatedExternalInterest = new ActionDataExternalInterest(
                actionTypeUuid,
                actionTypeLabel,
                caseType,
                null,
                "TEST_PARTY_TYPE_CHANGED",
                "TEST_DETAILS_CHANGED"
        );



        when(mockInfoClient.getCaseTypeActionByUuid(caseData.getType(), actionDataExternalInterestDto.getCaseTypeActionUuid()))
                .thenReturn(mockCaseTypeActionDto);
        when(mockCaseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(mockExternalInterestRepository.findByUuidAndCaseDataUuid(actionDataExternalInterestDto.getUuid(), caseUUID))
                .thenReturn(existingExternalInterestEntity);
        when(mockExternalInterestRepository.save(any(ActionDataExternalInterest.class)))
                .thenReturn(updatedExternalInterest);

        // WHEN
        actionDataExternalInterestService.update(caseUUID, stageUUID, actionEntityId, actionDataExternalInterestDto);

        verify(mockExternalInterestRepository, times(1)).save(externalInterestArgumentCaptor.capture());

        assertThat(externalInterestArgumentCaptor.getValue().getPartyType()).isEqualTo("TEST_PARTY_TYPE_CHANGED");
        assertThat(externalInterestArgumentCaptor.getValue().getDetailsOfInterest()).isEqualTo("TEST_DETAILS_CHANGED");

        verify(mockInfoClient, times(1)).getCaseTypeActionByUuid(eq(caseData.getType()), eq(actionTypeUuid));
        verify(mockAuditClient, times(1)).updateExternalInterestAudit(any());
        verify(mockCaseNoteService, times(1)).createCaseNote(eq(caseUUID), eq("UPDATE_INTEREST"), eq(actionTypeLabel));

    }
}
