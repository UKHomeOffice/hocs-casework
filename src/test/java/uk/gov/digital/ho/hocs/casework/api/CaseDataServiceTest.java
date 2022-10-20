package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseResponse;
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
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveCaseViewData;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActiveCaseViewDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseLinkRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    public static final Map<String, String> PREV_DATA_CLOB = new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
    }};

    private static final long caseID = 12345L;

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

    @Mock
    private CaseCopyFactory caseCopyFactory;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @Mock
    private DeadlineService deadlineService;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private CaseDataSummaryService caseDataSummaryService;

    Set<LocalDate> englandAndWalesBankHolidays2020 = Set.of(LocalDate.parse("2020-01-01"),
        LocalDate.parse("2020-04-10"), LocalDate.parse("2020-04-13"), LocalDate.parse("2020-05-08"),
        LocalDate.parse("2020-05-25"), LocalDate.parse("2020-08-31"), LocalDate.parse("2020-12-25"),
        LocalDate.parse("2020-12-28"));

    Set<BankHoliday.BankHolidayRegion> bankHolidayRegions = Set.of(BankHoliday.BankHolidayRegion.ENGLAND_AND_WALES);

    List<String> bankHolidayRegionsAsString = List.of("ENGLAND_AND_WALES");

    @Before
    public void setUp() {
        configuration = new SpringConfiguration();
        objectMapper = configuration.initialiseObjectMapper();
        this.caseDataService = new CaseDataService(caseDataRepository, activeCaseViewDataRepository, caseLinkRepository,
            infoClient, objectMapper, auditClient, caseCopyFactory, caseActionService, deadlineService, stageRepository,
            caseDataSummaryService);
    }

    @Test
    public void shouldCreateCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(deadlineService.calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla())).thenReturn(expectedDeadline);

        // when
        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), originalReceivedDate,
            null);

        // then
        verify(caseDataRepository).getNextSeriesId();
        verify(caseDataRepository).save(caseData);
        verify(deadlineService).calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla());

        assertThat(caseData.getCaseDeadline()).isEqualTo(expectedDeadline);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseUsingPreviousUUID() throws ApplicationExceptions.EntityCreationException {

        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");

        CaseDataType comp2 = new CaseDataType("display_name", "c6", "DISP", PREVIOUS_CASE_TYPE, 20, 15);

        CaseData previousCaseData = new CaseData(1L, PREVIOUS_CASE_UUID, LocalDateTime.now(), PREVIOUS_CASE_TYPE,
            PREVIOUS_CASE_REFERENCE, false, PREV_DATA_CLOB, UUID.randomUUID(),
            new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID), UUID.randomUUID(),
            new Correspondent(PREVIOUS_CASE_UUID, PREV_CORRESPONDENT_TYPE, PREV_FULLNAME, PREV_ORGANISATION,
                new Address(PREV_ADDR_1, PREV_ADDR_2, PREV_ADDR_3, PREV_ADDR_4, PREV_ADDR_5), PREV_TELEPHONE,
                PREV_EMAIL, PREV_REFERENCE, PREV_EXTERNAL_KEY), LocalDate.now(), LocalDate.now(),
            LocalDate.now().minusDays(10), false, Set.of(new ActiveStage(), new ActiveStage()),
            Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        when(caseDataRepository.findActiveByUuid(PREVIOUS_CASE_UUID)).thenReturn(previousCaseData);
        when(deadlineService.calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla())).thenReturn(expectedDeadline);

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(comp2);
        when(caseCopyFactory.getStrategy(any(), any())).thenReturn(Optional.of((fromCase, toCase) -> {}));

        // when
        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), originalReceivedDate,
            PREVIOUS_CASE_UUID);

        // then
        verify(caseDataRepository).findActiveByUuid(PREVIOUS_CASE_UUID);
        verify(caseDataRepository, times(0)).getNextSeriesId(); // ensure not used
        verify(caseDataRepository).save(caseData);
        ArgumentCaptor<CaseLink> caseLink = ArgumentCaptor.forClass(CaseLink.class);
        verify(caseLinkRepository).save(caseLink.capture());
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

        verify(deadlineService).calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla());

        // check deadline
        assertThat(caseData.getCaseDeadline()).isEqualTo(expectedDeadline);
    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(deadlineService.calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla())).thenReturn(expectedDeadline);

        // when
        CaseData caseData = caseDataService.createCase(caseType.getDisplayName(), null, originalReceivedDate, null);

        // then
        verify(caseDataRepository).getNextSeriesId();
        verify(caseDataRepository).save(caseData);

        verify(deadlineService).calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla());

        assertThat(caseData.getCaseDeadline()).isEqualTo(expectedDeadline);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate, null);

        verify(auditClient).createCaseAudit(caseData);
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

        verify(caseDataRepository).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldMigrateCase() throws ApplicationExceptions.EntityCreationException {

        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");

        CaseDataType newCaseType = new CaseDataType("display_name", "c6", "DISP", PREVIOUS_CASE_TYPE, 20, 15);

        CaseData previousCaseData = new CaseData(1L, PREVIOUS_CASE_UUID, LocalDateTime.now(), PREVIOUS_CASE_TYPE,
            PREVIOUS_CASE_REFERENCE, false, PREV_DATA_CLOB, UUID.randomUUID(),
            new Topic(PREVIOUS_CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID), UUID.randomUUID(),
            new Correspondent(PREVIOUS_CASE_UUID, PREV_CORRESPONDENT_TYPE, PREV_FULLNAME, PREV_ORGANISATION,
                new Address(PREV_ADDR_1, PREV_ADDR_2, PREV_ADDR_3, PREV_ADDR_4, PREV_ADDR_5), PREV_TELEPHONE,
                PREV_EMAIL, PREV_REFERENCE, PREV_EXTERNAL_KEY), LocalDate.now(), LocalDate.now(), originalReceivedDate,
            false, Set.of(new ActiveStage(), new ActiveStage()),
            Set.of(new CaseNote(UUID.randomUUID(), "type", "text", "author")));

        when(caseDataRepository.findActiveByUuid(PREVIOUS_CASE_UUID)).thenReturn(previousCaseData);
        when(deadlineService.calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla())).thenReturn(expectedDeadline);

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(newCaseType);
        when(caseCopyFactory.getStrategy(any(), any())).thenReturn(Optional.of((fromCase, toCase) -> {}));

        // when
        MigrateCaseResponse migrateCaseResponse = caseDataService.migrateCase(caseType.getDisplayCode(),
            PREVIOUS_CASE_UUID);

        // then
        verify(caseDataRepository).findActiveByUuid(PREVIOUS_CASE_UUID);
        verify(caseDataRepository, times(0)).getNextSeriesId(); // ensure not used
        ArgumentCaptor<CaseLink> caseLink = ArgumentCaptor.forClass(CaseLink.class);
        ArgumentCaptor<CaseData> caseData = ArgumentCaptor.forClass(CaseData.class);
        verify(caseLinkRepository).save(caseLink.capture());
        verify(caseDataRepository).save(caseData.capture());
        verifyNoMoreInteractions(caseDataRepository);

        // assert the save link values
        assertThat(caseLink.getValue()).isNotNull();
        CaseLink parameterUsed = caseLink.getValue();
        assertThat(parameterUsed.getPrimaryCase()).isEqualTo(PREVIOUS_CASE_UUID);
        assertThat(parameterUsed.getSecondaryCase()).isEqualTo(migrateCaseResponse.getUuid());

        // assert the reference matches expectations
        Matcher previousReferenceMatcher = CaseDataService.CASE_REFERENCE_PATTERN.matcher(PREVIOUS_CASE_REFERENCE);
        Matcher caseReferenceMatcher = CaseDataService.CASE_REFERENCE_PATTERN.matcher(
            caseData.getValue().getReference());

        // + assert the patterns are valid
        assertThat(previousReferenceMatcher.find()).isTrue();
        assertThat(caseReferenceMatcher.find()).isTrue();

        // assert the reference sequence number part is valid
        assertThat(previousReferenceMatcher.group(1)).isEqualTo(caseReferenceMatcher.group(1));

        verify(deadlineService).calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate,
            caseType.getSla());

        // check deadline
        assertThat(caseData.getValue().getCaseDeadline()).isEqualTo(expectedDeadline);

        // check audit
        verify(auditClient).migrateCaseAudit(caseData.getValue());
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseTimeline() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        Set<CaseNote> caseNoteData = Set.of(new CaseNote(caseUUID, "MANUAL", "case note 1", "a user"),
            new CaseNote(caseUUID, "MANUAL", "case note 2", "a user"));
        caseData.setCaseNotes(caseNoteData);

        UUID auditResponseUUID = UUID.randomUUID();
        Set<GetAuditResponse> auditResponse = Set.of(
            new GetAuditResponse(auditResponseUUID, caseUUID, null, "correlation Id", "hocs-casework", "", "namespace",
                ZonedDateTime.now(), CASE_CREATED.toString(), "user"));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any(), any())).thenReturn(auditResponse);

        List<TimelineItem> timeline = caseDataService.getCaseTimeline(caseData.getUuid()).collect(Collectors.toList());

        assertThat(timeline.size()).isEqualTo(3);

        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 2"));
        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 1"));
        assertThat(timeline).anyMatch(t -> t.getType().equals(CASE_CREATED.toString()));

        verify(auditClient).getAuditLinesForCase(eq(caseData.getUuid()), any(), any());
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseNotesOnlyTimelineOnAuditFailure() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        Set<CaseNote> caseNoteData = Set.of(new CaseNote(caseUUID, "MANUAL", "case note 1", "a user"),
            new CaseNote(caseUUID, "MANUAL", "case note 2", "a user"));
        caseData.setCaseNotes(caseNoteData);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any(), any())).thenThrow(
            new RuntimeException("Error"));

        List<TimelineItem> timeline = caseDataService.getCaseTimeline(caseData.getUuid()).collect(Collectors.toList());

        assertThat(timeline.size()).isEqualTo(2);

        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 2"));
        assertThat(timeline).anyMatch(t -> t.getMessage().contains("case note 1"));
        assertThat(timeline).noneMatch(t -> t.getType().equals(CASE_CREATED.toString()));

        verify(auditClient).getAuditLinesForCase(eq(caseData.getUuid()), any(), any());
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateTeamByStageAndTexts() {
        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Value1");
        data.put("Key2", "Value2");
        data.put("Key3", "Value3");

        CaseData caseData = new CaseData(caseType, caseID, data, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        TeamDto teamDto = new TeamDto("Team", UUID.randomUUID(), true, null);
        when(infoClient.getTeamByStageAndText("stageType", "Value1_Value2_Value3")).thenReturn(teamDto);
        String[] texts = { "Key1", "Key2", "Key3" };

        Map<String, String> teamMap = caseDataService.updateTeamByStageAndTexts(caseData.getUuid(), stageUUID,
            "stageType", "teamUUIDKey", "teamNameKey", texts);

        assertThat(teamMap).isNotNull();
        assertThat(teamMap.size()).isEqualTo(2);
        assertThat(teamMap.get("teamUUIDKey")).isEqualTo(teamDto.getUuid().toString());
        assertThat(teamMap.get("teamNameKey")).isEqualTo("Team");
    }

    @Test
    public void shouldGetCaseTeams() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        UUID auditResponseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.of(2019, 1, 1);
        LocalDate deadlineWarning = LocalDate.of(2020, 2, 1);

        String payload = objectMapper.writeValueAsString(
            new AuditPayload.StageAllocation(stageUUID, teamUUID, "STAGE_TYPE", deadline, deadlineWarning));

        Set<GetAuditResponse> auditResponse = Set.of(
            new GetAuditResponse(auditResponseUUID, caseUUID, UUID.randomUUID(), "correlation Id", "hocs-casework",
                payload, "namespace", ZonedDateTime.now(), STAGE_ALLOCATED_TO_TEAM.toString(), "user"));

        when(auditClient.getAuditLinesForCase(eq(caseData.getUuid()), any())).thenReturn(auditResponse);

        Set<UUID> teams = caseDataService.getCaseTeams(caseData.getUuid());

        assertThat(teams.size()).isEqualTo(1);
        assertThat(teams).contains(teamUUID);

        verify(auditClient).getAuditLinesForCase(eq(caseData.getUuid()), any());
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParams() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, deadlineDate);
        activeCaseViewData.setCaseDeadline(caseDeadline);
        activeCaseViewData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = Map.of("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
            "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType(caseData.getType(),
            caseData.getDataMap())).thenReturn(Set.of());
        when(
            deadlineService.getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived())).thenReturn(
            deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(caseDataSummaryService).getAdditionalCaseDataFieldsByCaseType(caseData.getType(), caseData.getDataMap());
        verify(deadlineService).getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseSummaryWithOverride() throws ApplicationExceptions.EntityNotFoundException {

        LocalDate overrideDeadline = LocalDate.now().plusDays(7);
        Map<String, String> data = new HashMap<>();
        data.put("DCU_DTEN_COPY_NUMBER_TEN_DEADLINE", overrideDeadline.toString());

        CaseData caseData = new CaseData(caseType, caseID, data, deadlineDate);
        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, data, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = Map.of("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
            "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType(caseData.getType(),
            caseData.getDataMap())).thenReturn(Set.of());
        when(
            deadlineService.getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived())).thenReturn(
            deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isNotEqualTo(deadlines);
        assertThat(result.getStageDeadlines().get("DCU_DTEN_COPY_NUMBER_TEN")).isEqualTo(overrideDeadline.toString());
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(caseDataSummaryService).getAdditionalCaseDataFieldsByCaseType(caseData.getType(), caseData.getDataMap());
        verify(deadlineService).getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentNull() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(null);

        Map<String, LocalDate> deadlines = Map.of("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
            "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType(caseData.getType(),
            caseData.getDataMap())).thenReturn(Set.of());
        when(
            deadlineService.getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived())).thenReturn(
            deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(caseDataSummaryService).getAdditionalCaseDataFieldsByCaseType(caseData.getType(), caseData.getDataMap());
        verify(deadlineService).getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());

    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentException() throws ApplicationExceptions.EntityNotFoundException {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        ActiveCaseViewData activeCaseViewData = new ActiveCaseViewData(caseType, caseID, deadlineDate);
        activeCaseViewData.setCaseDeadline(caseDeadline);
        activeCaseViewData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = Map.of("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
            "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        when(activeCaseViewDataRepository.findByUuid(caseData.getUuid())).thenReturn(activeCaseViewData);
        when(caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType(caseData.getType(),
            caseData.getDataMap())).thenReturn(Set.of());
        when(
            deadlineService.getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived())).thenReturn(
            deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(caseDataSummaryService).getAdditionalCaseDataFieldsByCaseType(caseData.getType(), caseData.getDataMap());
        verify(deadlineService).getAllStageDeadlinesForCaseType(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseWithValidParams() throws ApplicationExceptions.EntityNotFoundException {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());

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

        verify(caseDataRepository).findActiveByUuid(caseUUID);

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

        verify(caseDataRepository).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCalculateTotals() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        EntityTotalDto entityTotalDto = new EntityTotalDto(new HashMap(), new HashMap());
        EntityDto<EntityTotalDto> entityDto = new EntityDto<EntityTotalDto>("simpleName", entityTotalDto);
        List<EntityDto<EntityTotalDto>> entityListTotals = new ArrayList();
        entityListTotals.add(entityDto);
        when(infoClient.getEntityListTotals("list")).thenReturn(entityListTotals);

        Map<String, String> totals = caseDataService.calculateTotals(caseData.getUuid(), stageUUID, "list");

        assertThat(totals).isNotNull();
        verify(caseDataRepository, times(2)).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateCase() throws JsonProcessingException {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditUpdateCase() throws ApplicationExceptions.EntityCreationException {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, null);

        verifyNoInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotUpdateCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.updateCaseData((UUID) null, stageUUID, new HashMap<>());
    }

    @Test()
    public void shouldNotUpdateCaseMissingCaseUUID() throws JsonProcessingException {

        try {
            caseDataService.updateCaseData((UUID) null, stageUUID, new HashMap<>());
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateDateReceived() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-01-01");
        LocalDate updatedReceivedDate = LocalDate.parse("2020-01-04");
        LocalDate expectedNewDeadline = LocalDate.parse("2020-02-03");

        CaseData caseData = new CaseData(caseType, caseID, originalReceivedDate);
        CaseDataType caseDataType = new CaseDataType("MIN", "TT", "TEST_TYPE", null, 20, 15);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseType(any())).thenReturn(caseDataType);
        when(deadlineService.calculateWorkingDaysForCaseType(eq(caseDataType.getDisplayName()), eq(updatedReceivedDate),
            anyInt())).thenReturn(expectedNewDeadline);

        // when
        caseDataService.updateDateReceived_defaultSla(caseUUID, stageUUID, updatedReceivedDate);

        // then
        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        assertThat(caseData.getDateReceived()).isEqualTo(updatedReceivedDate);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(caseDataCaptor.capture());
        verify(deadlineService).calculateWorkingDaysForCaseType(caseDataType.getDisplayName(), updatedReceivedDate,
            caseType.getSla());

        assertThat(caseDataCaptor.getValue().getCaseDeadline()).isEqualTo(expectedNewDeadline);

        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
    }

    @Test
    public void shouldOverrideSla() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedNewDeadline = LocalDate.parse("2020-02-17");

        CaseData caseData = new CaseData(caseType, caseID, originalReceivedDate);
        CaseDataType caseDataType = new CaseDataType("MIN", "TT", "TEST_TYPE", null, 20, 15);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(deadlineService.calculateWorkingDaysForCaseType(caseData.getType(), caseData.getDateReceived(),
            10)).thenReturn(expectedNewDeadline);

        // when
        caseDataService.overrideSla(caseUUID, stageUUID, 10);

        // then
        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(caseDataCaptor.capture());
        verify(deadlineService, times(2)).calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), 10);

        assertThat(caseDataCaptor.getValue().getCaseDeadline()).isEqualTo(expectedNewDeadline);

        verifyNoMoreInteractions(caseDataRepository);

        verify(auditClient).updateCaseAudit(caseData, stageUUID);
    }

    @Test
    public void shouldUpdateDispatchDeadlineDate() {

        // given
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseType(caseType.getDisplayName())).thenReturn(
            new CaseDataType(null, null, null, null, 20, 15));
        when(deadlineService.calculateWorkingDaysForCaseType(any(), any(), eq(15))).thenReturn(LocalDate.now());
        // when
        caseDataService.updateDispatchDeadlineDate(caseUUID, stageUUID, deadlineDate);

        // then
        assertThat(caseData.getCaseDeadline()).isEqualTo(deadlineDate);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(deadlineService).calculateWorkingDaysForCaseType(any(), any(), eq(15));
        verify(auditClient).updateCaseAudit(caseData, stageUUID);

    }

    @Test
    public void shouldUpdateStageDeadline() {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();

        when(deadlineService.calculateWorkingDaysForCaseType(any(), any(), eq(7))).thenReturn(LocalDate.now());

        caseDataService.updateStageDeadline(caseData.getUuid(), stageUUID, "TEST", 7);

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verify(deadlineService).calculateWorkingDaysForCaseType(any(), any(), eq(7));
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldCompleteCaseWhenNoFinalActiveStage() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(stageRepository.findFirstByTeamUUIDIsNotNullAndCaseUUID(caseData.getUuid())).thenReturn(Optional.empty());
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.completeCase(caseData.getUuid(), true);

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(stageRepository).findFirstByTeamUUIDIsNotNullAndCaseUUID(any(UUID.class));
        verify(caseDataRepository).save(caseData);

        // Not invoked
        verify(stageRepository, times(0)).save(any(Stage.class));
        verify(auditClient, times(0)).updateStageTeam(any(Stage.class));

        verifyNoMoreInteractions(caseDataRepository, stageRepository);
    }

    @Test
    public void shouldCompleteStageAndCaseWhenFinalActiveStage() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        Stage mockStage = new Stage(caseData.getUuid(), "RANDOM_STAGE_TYPE", UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID());
        Optional<Stage> mockOptionalOfStage = Optional.of(mockStage);

        when(stageRepository.findFirstByTeamUUIDIsNotNullAndCaseUUID(caseData.getUuid())).thenReturn(
            mockOptionalOfStage);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.completeCase(caseData.getUuid(), true);

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verify(stageRepository).findFirstByTeamUUIDIsNotNullAndCaseUUID(any(UUID.class));
        verify(stageRepository).save(any(Stage.class));
        verify(auditClient).updateStageTeam(any(Stage.class));

        verifyNoMoreInteractions(caseDataRepository, stageRepository);
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

        verify(caseDataRepository).findActiveByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findAnyByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid(), true);

        verify(caseDataRepository).findAnyByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verify(auditClient).deleteAuditLinesForCase(eq(caseData.getUuid()), any(), eq(true));
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditDeleteCase() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findAnyByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid(), true);

        verify(auditClient).deleteCaseAudit(caseData, true);
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

        verify(caseDataRepository).findAnyByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenReturn(
            CaseDataTypeFactory.from("MIN", "a1", "COMP"));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test
    public void shouldReturnCaseTypeWhenNullReturnedFromInfoClientAndButCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenThrow(RestClientException.class);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(
            new CaseData(CaseDataTypeFactory.from("", ""), 1L, null));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
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

        verify(infoClient).clearCachedTemplateForCaseType(caseType.getDisplayName());
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.ofEntries(Map.entry("type1", 5), Map.entry("type2", 10),
            Map.entry("type3", 9));

        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();
        when(deadlineService.calculateWorkingDaysForCaseType(eq(caseType.getDisplayCode()), eq(deadlineDate),
            eq(9))).thenReturn(caseDeadline);
        when(deadlineService.calculateWorkingDaysForCaseType(eq(caseType.getDisplayCode()), eq(deadlineDate),
            eq(10))).thenReturn(caseDeadline);
        when(deadlineService.calculateWorkingDaysForCaseType(eq(caseType.getDisplayCode()), eq(deadlineDate),
            eq(5))).thenReturn(caseDeadline);

        caseDataService.updateDeadlineForStages(caseData.getUuid(), stageUUID, stageTypeAndDaysMap);

        verify(deadlineService).calculateWorkingDaysForCaseType(any(), any(), eq(9));
        verify(deadlineService).calculateWorkingDaysForCaseType(any(), any(), eq(10));
        verify(deadlineService).calculateWorkingDaysForCaseType(any(), any(), eq(5));

        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void getCaseDataByReference() {
        String testCaseRef = "TestReference";
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(caseData);

        CaseData result = caseDataService.getCaseDataByReference(testCaseRef);

        assertThat(result).isEqualTo(caseData);
        verify(caseDataRepository).findByReference(testCaseRef);
        checkNoMoreInteractions();

    }

    @Test
    public void getCaseReferenceByUUID() {
        CaseData caseData = new CaseData(caseType, caseID, deadlineDate);

        when(caseDataRepository.findActiveByUuid(caseData.getUuid())).thenReturn(caseData);

        String result = caseDataService.getCaseRef(caseData.getUuid());

        assertThat(result).isEqualTo(caseData.getReference());
        verify(caseDataRepository).findActiveByUuid(caseData.getUuid());
        checkNoMoreInteractions();
    }

    @Test
    public void getCaseReferenceByUUIDNull() {
        UUID caseUUID = UUID.randomUUID();
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(null);

        String result = caseDataService.getCaseRef(caseUUID);

        assertThat(result).isEqualTo("REFERENCE NOT FOUND");
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        checkNoMoreInteractions();
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getCaseDataByReference_forNullResult() {
        String testCaseRef = "TestReference";
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(null);

        caseDataService.getCaseDataByReference(testCaseRef);

    }

    @Test
    public void testShouldMapCaseDataValues() {
        // GIVEN
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("from1", "to1");
        keyMappings.put("from2", "to2");

        Map<String, String> caseDataMap = new HashMap<>();
        caseDataMap.put("from1", "val1");
        caseDataMap.put("from2", "val2");
        caseDataMap.put("from3", "val3");

        CaseDataType caseType = new CaseDataType("CASE_TYPE", "a9", "ct", null, 20, 20);
        long caseNumber = 1L;
        LocalDate dateReceived = LocalDate.now();

        CaseData caseData = new CaseData(caseType, caseNumber, caseDataMap, dateReceived);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        caseDataService.mapCaseDataValues(caseUUID, keyMappings);
        // THEN
        ArgumentCaptor<CaseData> argCapture = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(argCapture.capture());
        verify(auditClient).updateCaseAudit(caseData, null);
        verifyNoMoreInteractions(caseDataRepository, auditClient);

        assertThat(argCapture.getValue().getDataMap().keySet()).contains("to1", "to2");
        assertThat(argCapture.getValue().getDataMap().get("to1")).isEqualTo("val1");
        assertThat(argCapture.getValue().getDataMap().get("to2")).isEqualTo("val2");
    }

    @Test
    public void testShouldNotMapIfMapToKeyExistsInCaseDataButShouldMapOthers() {
        // GIVEN
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("from1", "to1");
        keyMappings.put("from2", "to2");
        keyMappings.put("from3", "to3");

        Map<String, String> caseDataMap = new HashMap<>();
        caseDataMap.put("from1", "val1");
        caseDataMap.put("from2", "val2");
        caseDataMap.put("from3", "val3");

        // existing requested map to
        caseDataMap.put("to3", "val3Existing");

        CaseDataType caseType = new CaseDataType("CASE_TYPE", "a9", "ct", null, 20, 20);
        long caseNumber = 1L;
        LocalDate dateReceived = LocalDate.now();

        CaseData caseData = new CaseData(caseType, caseNumber, caseDataMap, dateReceived);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        caseDataService.mapCaseDataValues(caseUUID, keyMappings);
        // THEN
        ArgumentCaptor<CaseData> argCapture = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(argCapture.capture());
        verify(auditClient).updateCaseAudit(caseData, null);
        verifyNoMoreInteractions(caseDataRepository, auditClient);

        assertThat(argCapture.getValue().getDataMap().keySet()).contains("to1", "to2", "to3");
        assertThat(argCapture.getValue().getDataMap().get("to1")).isEqualTo("val1");
        assertThat(argCapture.getValue().getDataMap().get("to2")).isEqualTo("val2");
        assertThat(argCapture.getValue().getDataMap().get("to3")).isEqualTo("val3Existing");

    }

    @Test
    public void testShouldMapIfMapToKeyExistsInCaseDataButIsNull() {
        // GIVEN
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("from1", "to1");
        keyMappings.put("from2", "to2");
        keyMappings.put("from3", "to3");

        Map<String, String> caseDataMap = new HashMap<>();
        caseDataMap.put("from1", "val1");
        caseDataMap.put("from2", "val2");
        caseDataMap.put("from3", "val3");

        // existing requested map to
        caseDataMap.put("to3", null);

        CaseDataType caseType = new CaseDataType("CASE_TYPE", "a9", "ct", null, 20, 20);
        long caseNumber = 1L;
        LocalDate dateReceived = LocalDate.now();

        CaseData caseData = new CaseData(caseType, caseNumber, caseDataMap, dateReceived);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        caseDataService.mapCaseDataValues(caseUUID, keyMappings);
        // THEN
        ArgumentCaptor<CaseData> argCapture = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository).save(argCapture.capture());
        verify(auditClient).updateCaseAudit(caseData, null);
        verifyNoMoreInteractions(caseDataRepository, auditClient);

        assertThat(argCapture.getValue().getDataMap().keySet()).contains("to1", "to2", "to3");
        assertThat(argCapture.getValue().getDataMap().get("to1")).isEqualTo("val1");
        assertThat(argCapture.getValue().getDataMap().get("to2")).isEqualTo("val2");
        assertThat(argCapture.getValue().getDataMap().get("to3")).isEqualTo("val3");

    }

    @Test(expected = ApplicationExceptions.DataMappingException.class)
    public void testShouldThrowExceptionIfReqeustedMappingsAreNotInCaseDataValues() {
        // GIVEN
        Map<String, String> keyMappings = new HashMap<>();
        keyMappings.put("from1", "to1");
        keyMappings.put("from2", "to2");

        Map<String, String> caseDataMap = new HashMap<>();
        caseDataMap.put("from3", "val3");
        caseDataMap.put("from4", "val4");
        caseDataMap.put("from5", "val5");

        CaseDataType caseType = new CaseDataType("CASE_TYPE", "a9", "ct", null, 20, 20);
        long caseNumber = 1L;
        LocalDate dateReceived = LocalDate.now();

        CaseData caseData = new CaseData(caseType, caseNumber, caseDataMap, dateReceived);

        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);

        // WHEN
        caseDataService.mapCaseDataValues(caseUUID, keyMappings);
        // THEN
        ArgumentCaptor<CaseData> argCapture = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
        verify(caseDataRepository, times(0)).save(any());
        verify(auditClient, times(0)).updateCaseAudit(any(), any());

        verifyNoMoreInteractions(caseDataRepository, auditClient);

    }

    // HELPERS
    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(auditClient, caseDataRepository, infoClient);
    }

}
