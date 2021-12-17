package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopyFactory;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityTotalDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito. verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_TEAM;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

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
    private static final long caseID = 12345L;

    private static final String OFFLINE_QA_USER = "OfflineQaUser";
    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");
    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final UUID primaryCorrespondentUUID = UUID.randomUUID();
    private final String caseRef = "TestRef";
    private final LocalDate caseDeadline = LocalDate.now().plusDays(20);
    private final LocalDate caseDeadlineWarning = LocalDate.now().plusDays(15);
    private final LocalDate deadlineDate = LocalDate.now();

    private CaseDataService caseDataService;
    private ObjectMapper objectMapper;
    private SpringConfiguration configuration;

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private ActiveCaseViewDataRepository activeCaseViewDataRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private CaseActionService caseActionService;

    private LocalDate caseDeadlineExtended = LocalDate.now().plusDays(45);
    private LocalDate caseReceived = LocalDate.now();

    @Captor
    ArgumentCaptor<CaseData> caseDataCaptor;

    @Spy
    ActiveStage activeStage = new ActiveStage();

    @Mock
    private CaseCopyFactory caseCopyFactory;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @Before
    public void setUp() {
        configuration = new SpringConfiguration();
        objectMapper = configuration.initialiseObjectMapper();
        this.caseDataService = new CaseDataService(caseDataRepository,
                activeCaseViewDataRepository,
                caseLinkRepository,
                infoClient,
                objectMapper,
                auditClient,
                caseCopyFactory,
                caseActionService
        );
    }

    @Test
    public void shouldCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate, null);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0);
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseUsingPreviousUUID() throws ApplicationExceptions.EntityCreationException {

        // given
        CaseDataType comp2 = new CaseDataType("display_name",
                "c6", "DISP", PREVIOUS_CASE_TYPE);

        CaseData previousCaseData = new CaseData(
                1L,
                PREVIOUS_CASE_UUID,
                LocalDateTime.now(),
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
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().minusDays(10),
                false,
                Set.of(new ActiveStage(), new ActiveStage()),
                Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));


        when(caseDataRepository.findActiveByUuid(PREVIOUS_CASE_UUID)).thenReturn(previousCaseData);

        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(comp2);
        when(caseCopyFactory.getStrategy(any(), any())).thenReturn(Optional.of((fromCase, toCase) -> {}));

        // when
        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate, PREVIOUS_CASE_UUID);

        // then
        verify(caseDataRepository, times(1)).findActiveByUuid(PREVIOUS_CASE_UUID);
        verify(caseDataRepository, times(0)).getNextSeriesId(); // ensure not used
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0);
        verify(caseDataRepository, times(1)).save(caseData);
        ArgumentCaptor<CaseLink> caseLink = ArgumentCaptor.forClass(CaseLink.class);
        verify(caseLinkRepository, times(1)).save(caseLink.capture());
        verifyNoMoreInteractions(caseDataRepository);

        // assert the save link values
        assertThat(caseLink.getValue()).isNotNull();
        CaseLink parameterUsed = caseLink.getValue();
        assertThat(parameterUsed.getPrimaryCase()).isEqualTo(PREVIOUS_CASE_UUID);
        assertThat(parameterUsed.getSecondaryCase()).isEqualTo(caseData.getUuid());

        // assert the reference matches expectations
        Matcher previousReferenceMatcher = CaseDataService.CASE_REFERENCE_PATTERN.matcher(PREVIOUS_CASE_REFERENCE);
        Matcher caseReferenceMatcher = CaseDataService.CASE_REFERENCE_PATTERN.matcher(caseData.getReference());

        // + assert the patterns are valid
        assertThat(previousReferenceMatcher.find()).isTrue();
        assertThat(caseReferenceMatcher.find()).isTrue();

        // assert the reference sequence number part is valid
        assertThat(previousReferenceMatcher.group(1)).isEqualTo(caseReferenceMatcher.group(1));

    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayName(), null, deadlineDate, null);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0);
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate, null);

        verify(auditClient, times(1)).createCaseAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.createCase(null, new HashMap<>(), deadlineDate, null);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null, new HashMap<>(), deadlineDate, null);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetDocumentTags() {
        UUID caseUUID = UUID.randomUUID();
        when(caseDataRepository.getCaseType(caseUUID)).thenReturn("TEST");
        List<String> documentTags = new ArrayList<String>(Arrays.asList("Tag"));
        when(infoClient.getDocumentTags("TEST")).thenReturn(documentTags);

        List<String> tags = caseDataService.getDocumentTags(caseUUID);

        assertThat(tags).isSameAs(documentTags);
        verify(caseDataRepository).getCaseType(caseUUID);
        verifyNoMoreInteractions(caseDataRepository);
        verify(infoClient).getDocumentTags("TEST");
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldGetCaseTimeline() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        Set<CaseNote> caseNoteData = Set.of(
                new CaseNote(caseUUID, "MANUAL", "case note 1", "a user"),
                new CaseNote(caseUUID, "MANUAL", "case note 2", "a user"));
        caseData.setCaseNotes(caseNoteData);

        UUID auditResponseUUID = UUID.randomUUID();
        Set<GetAuditResponse> auditResponse = Set.of(new GetAuditResponse(auditResponseUUID,
                caseUUID,
                null,
                "correlation Id",
                "hocs-casework", "",
                "namespace", ZonedDateTime.now(), CASE_CREATED.toString(),
                "user"));


        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any())).thenReturn(auditResponse);

        List<TimelineItem> timeline = caseDataService.getCaseTimeline(caseData.getUuid()).collect(Collectors.toList());


        assertThat(timeline.size()).isEqualTo(3);

        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 2"));
        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 1"));
        assertThat(timeline).anyMatch(t -> t.getType().equals(CASE_CREATED.toString()));

        verify(auditClient, times(1)).getAuditLinesForCase(eq(caseData.getUuid()), any());
        verifyNoMoreInteractions(auditClient);
    }


    @Test
    public void shouldGetCaseNotesOnlyTimelineOnAuditFailure() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        Set<CaseNote> caseNoteData = Set.of(
                new CaseNote(caseUUID, "MANUAL", "case note 1", "a user"),
                new CaseNote(caseUUID, "MANUAL", "case note 2", "a user"));
        caseData.setCaseNotes(caseNoteData);


        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any())).thenThrow(new RuntimeException("Error"));

        List<TimelineItem> timeline = caseDataService.getCaseTimeline(caseData.getUuid()).collect(Collectors.toList());

        assertThat(timeline.size()).isEqualTo(2);

        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 2"));
        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 1"));
        assertThat(timeline).noneMatch(t -> t.getType().equals(CASE_CREATED.toString()));

        verify(auditClient, times(1)).getAuditLinesForCase(eq(caseData.getUuid()), any());
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateTeamByStageAndTexts() {
        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Value1");
        data.put("Key2", "Value2");
        data.put("Key3", "Value3");
        CaseData caseData = new CaseData(caseType, caseID, data, objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        TeamDto teamDto = new TeamDto("Team", UUID.randomUUID(), true, null);
        when(infoClient.getTeamByStageAndText("stageType", "Value1_Value2_Value3")).thenReturn(teamDto);
        String[] texts = {"Key1", "Key2", "Key3"};

        Map<String, String> teamMap = caseDataService.updateTeamByStageAndTexts(
                caseData.getUuid(), stageUUID, "stageType", "teamUUIDKey", "teamNameKey", texts);

        assertThat(teamMap).isNotNull();
        assertThat(teamMap.size()).isEqualTo(2);
        assertThat(teamMap.get("teamUUIDKey")).isEqualTo(teamDto.getUuid().toString());
        assertThat(teamMap.get("teamNameKey")).isEqualTo("Team");
    }

    @Test
    public void shouldGetCaseTeams() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        UUID auditResponseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.of(2019, 1, 1);
        LocalDate deadlineWarning = LocalDate.of(2020, 2, 1);

        String payload = objectMapper.writeValueAsString(new AuditPayload.StageAllocation(stageUUID, teamUUID, "STAGE_TYPE", deadline, deadlineWarning));

        Set<GetAuditResponse> auditResponse = Set.of(new GetAuditResponse(auditResponseUUID,
                caseUUID,
                UUID.randomUUID(),
                "correlation Id",
                "hocs-casework",
                payload,
                "namespace", ZonedDateTime.now(),
                STAGE_ALLOCATED_TO_TEAM.toString(),
                "user"));

        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any())).thenReturn(auditResponse);

        Set<UUID> teams = caseDataService.getCaseTeams(caseData.getUuid());

        assertThat(teams.size()).isEqualTo(1);
        assertThat(teams).contains(teamUUID);

        verify(auditClient, times(1)).getAuditLinesForCase(eq(caseData.getUuid()), any());
        verifyNoMoreInteractions(auditClient);
    }


    @Test
    public void shouldGetCaseSummaryWithValidParams() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        activeCaseViewData.setCaseDeadline(caseDeadline);
        activeCaseViewData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseSummaryWithOverride() throws ApplicationExceptions.EntityNotFoundException, IOException {

        LocalDate overrideDeadline = LocalDate.now().plusDays(7);
        Map<String, String> data = new HashMap<>();
        data.put("DCU_DTEN_COPY_NUMBER_TEN_DEADLINE", overrideDeadline.toString());

        CaseData caseData = new CaseData(caseType, caseID, data, objectMapper, deadlineDate);
        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, data, objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isNotEqualTo(deadlines);
        assertThat(result.getStageDeadlines().get("DCU_DTEN_COPY_NUMBER_TEN")).isEqualTo(overrideDeadline.toString());
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient).getCaseSummaryFields(caseData.getType());
        verify(infoClient).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldAuditGetCaseSummary() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary caseSummary = caseDataService.getCaseSummary(caseData.getUuid());

        verify(auditClient, times(1)).viewCaseSummaryAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentNull() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(null);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());

    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentException() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        ActiveCaseViewData activeCaseViewData =
                new ActiveCaseViewData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        activeCaseViewData.setCaseDeadline(caseDeadline);
        activeCaseViewData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Set<FieldDto> filterFields = new HashSet<>();


        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseOnlyFilteredAdditionalData() throws ApplicationExceptions.EntityNotFoundException, IOException {

        Set<FieldDto> filterFields = new HashSet<>();

        FieldDto field0 = new FieldDto(UUID.randomUUID(), "TEMPCReference", "what is your TEMPCReference", "Text", new String[]{}, true, true, null);
        filterFields.add(field0);

        FieldDto field1 = new FieldDto(UUID.randomUUID(), "CopyNumberTen", "what is your CopyNumberTen", "Text", new String[]{}, true, true, null);
        filterFields.add(field1);


        Map<String, String> additionalData = Map.of(
                "TEMPCReference", "test ref",
                "CopyNumberTen", "true",
                "UnfilteredField", "some value"
        );

        CaseData caseData = new CaseData(caseType, caseID, additionalData, objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, additionalData, objectMapper, deadlineDate);
        activeCaseViewData.setCaseDeadline(caseDeadline);
        activeCaseViewData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getAdditionalFields().stream().filter(f -> f.getLabel().equals("what is your TEMPCReference")).findFirst().get().getValue()).isEqualTo("test ref");
        assertThat(result.getAdditionalFields().stream().filter(f -> f.getLabel().equals("what is your CopyNumberTen")).findFirst().get().getValue()).isEqualTo("true");
        assertThat(result.getAdditionalFields().stream().noneMatch(f -> f.getLabel().equals("UnfilteredField")));
    }

    @Test
    public void shouldGetCaseWithValidParams() throws ApplicationExceptions.EntityNotFoundException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCase(caseUUID);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        try {
            caseDataService.getCase(caseUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findActiveByUuid(caseUUID);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws ApplicationExceptions.EntityNotFoundException {
        caseDataService.getCase(null);
    }

    @Test
    public void shouldNotGetCaseMissingUUID() {

        try {
            caseDataService.getCase(null);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCalculateTotals() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        EntityTotalDto entityTotalDto = new EntityTotalDto(new HashMap(), new HashMap());
        EntityDto<EntityTotalDto> entityDto = new EntityDto<EntityTotalDto>("simpleName", entityTotalDto);
        List<EntityDto<EntityTotalDto>> entityListTotals = new ArrayList();
        entityListTotals.add(entityDto);
        when(infoClient.getEntityListTotals("list")).thenReturn(entityListTotals);

        Map<String, String> totals = caseDataService.calculateTotals(caseData.getUuid(), stageUUID, "list");

        assertThat(totals).isNotNull();
        verify(caseDataRepository, times(2)).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateCase() throws JsonProcessingException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditUpdateCase() throws ApplicationExceptions.EntityCreationException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, null);

         verifyNoInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotUpdateCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.updateCaseData((UUID)null, stageUUID, new HashMap<>());
    }

    @Test()
    public void shouldNotUpdateCaseMissingCaseUUID() throws JsonProcessingException {

        try {
            caseDataService.updateCaseData((UUID)null, stageUUID, new HashMap<>());
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }


    @Test
    public void shouldUpdateDateReceived() {

        // given
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadlineWarning);

        // when
        caseDataService.updateDateReceived(caseUUID, stageUUID, deadlineDate, 0);

        // then
        assertThat(caseData.getDateReceived()).isEqualTo(deadlineDate);
        verify(caseDataRepository, times(1)).findActiveByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);

        verify(infoClient, times(1)).getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 0);
        verify(infoClient, times(1)).getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);

        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);

    }


    @Test
    public void shouldUpdateDispatchDeadlineDate() {

        // given
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadlineWarning);

        // when
        caseDataService.updateDispatchDeadlineDate(caseUUID, stageUUID, deadlineDate);

        // then
        assertThat(caseData.getCaseDeadline()).isEqualTo(deadlineDate);
        verify(caseDataRepository, times(1)).findActiveByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);

        verify(infoClient, times(1)).getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);
        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);

    }


    @Test
    public void shouldUpdateStageDeadline() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 7)).thenReturn(caseDeadline);

        caseDataService.updateStageDeadline(caseData.getUuid(), stageUUID, "TEST", 7);

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldCompleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.completeCase(caseData.getUuid(), true);

        verify(caseDataRepository, times(1)).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotCOmpleteCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.completeCase(null, true);
    }

    @Test()
    public void shouldNotCompleteCaseMissingCaseUUID() {

        try {
            caseDataService.completeCase(null, true);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findAnyByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid(), true);

        verify(caseDataRepository, times(1)).findAnyByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);
        verify(auditClient).deleteAuditLinesForCase(eq(caseData.getUuid()), any(), eq(true));
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditDeleteCase() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findAnyByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid(), true);

        verify(auditClient, times(1)).deleteCaseAudit(caseData, true);
        verify(auditClient).deleteAuditLinesForCase(eq(caseData.getUuid()), any(), eq(true));
        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.deleteCase(null, true);
    }

    @Test()
    public void shouldNotDeleteCaseMissingCaseUUID() {

        try {
            caseDataService.deleteCase(null, true);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findAnyByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenReturn(CaseDataTypeFactory.from("MIN", "a1", "COMP"));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test
    public void shouldReturnCaseTypeWhenNullReturnedFromInfoClientAndButCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenThrow(RestClientException.class);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(new CaseData(CaseDataTypeFactory.from("", ""), 1L, null));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verify(caseDataRepository, times(1)).findActiveByUuid(caseUUID);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionWhenNullReturnedFromInfoClientAndNoCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenThrow(RestClientException.class);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCaseType(caseUUID);
    }

    @Test
    public void shouldEvictFromTheCache() {

        caseDataService.clearCachedTemplateForCaseType(caseType.getDisplayName());

        verify(infoClient, times(1)).clearCachedTemplateForCaseType(caseType.getDisplayName());
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.ofEntries(Map.entry("type1", 5),
                Map.entry("type2", 10), Map.entry("type3", 9));

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();

        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 5)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 10)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 9)).thenReturn(caseDeadline);

        caseDataService.updateDeadlineForStages(caseData.getUuid(), stageUUID, stageTypeAndDaysMap);

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void getCaseDataByReference() {
        String testCaseRef = "TestReference";
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(caseData);

        CaseData result = caseDataService.getCaseDataByReference(testCaseRef);

        assertThat(result).isEqualTo(caseData);
        verify(caseDataRepository).findByReference(testCaseRef);
        checkNoMoreInteractions();

    }


    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getCaseDataByReference_forNullResult() {
        String testCaseRef = "TestReference";
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(null);

        caseDataService.getCaseDataByReference(testCaseRef);

    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(auditClient, caseDataRepository, infoClient);
    }
}
