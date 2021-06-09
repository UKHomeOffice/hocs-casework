package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityTotalDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDeadlineExtensionTypeRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_TEAM;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    private static final long caseID = 12345L;
    
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
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
    private InfoClient infoClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private CaseDeadlineExtensionTypeRepository caseDeadlineExtensionTypeRepository;

    private LocalDate caseDeadlineExtended = LocalDate.now().plusDays(45);
    private LocalDate caseReceived = LocalDate.now();

    @Captor
    ArgumentCaptor<CaseData> caseDataCaptor;

    @Before
    public void setUp() {
        configuration = new SpringConfiguration();
        objectMapper = configuration.initialiseObjectMapper();
        this.caseDataService = new CaseDataService(caseDataRepository, infoClient, objectMapper, auditClient, caseDeadlineExtensionTypeRepository);
    }

    @Test
    public void shouldCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0);
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), deadlineDate, 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayName(), null, deadlineDate);

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

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), deadlineDate);

        verify(auditClient, times(1)).createCaseAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.createCase(null, new HashMap<>(), deadlineDate);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null, new HashMap<>(), deadlineDate);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetDocumentTags(){
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
    public void shouldGetCaseTimeline()  {
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
                "hocs-casework","",
                "namespace", ZonedDateTime.now(), CASE_CREATED.toString(),
                "user"));


        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
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
    public void shouldGetCaseNotesOnlyTimelineOnAuditFailure()  {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        Set<CaseNote> caseNoteData = Set.of(
                new CaseNote(caseUUID, "MANUAL", "case note 1", "a user"),
                new CaseNote(caseUUID, "MANUAL", "case note 2", "a user"));
        caseData.setCaseNotes(caseNoteData);


        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
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
    public void shouldUpdateTeamByStageAndTexts(){
        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Value1");
        data.put("Key2", "Value2");
        data.put("Key3", "Value3");
        CaseData caseData = new CaseData(caseType, caseID, data, objectMapper, deadlineDate);
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        TeamDto teamDto = new TeamDto("Team", UUID.randomUUID(), true, null);
        when(infoClient.getTeamByStageAndText("stageType", "Value1_Value2_Value3")).thenReturn(teamDto);
        String[] texts = { "Key1", "Key2", "Key3" };

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
        LocalDate deadline = LocalDate.of(2019,1,1);
        LocalDate deadlineWarning = LocalDate.of(2020,2,1);

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
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseSummaryWithOverride() throws ApplicationExceptions.EntityNotFoundException, IOException {

        LocalDate overrideDeadline = LocalDate.now().plusDays(7);
        Map<String, String> data = new HashMap<>();
        data.put("DCU_DTEN_COPY_NUMBER_TEN_DEADLINE", overrideDeadline.toString());

        CaseData caseData = new CaseData(caseType, caseID, data, objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isNotEqualTo(deadlines);
        assertThat(result.getStageDeadlines().get("DCU_DTEN_COPY_NUMBER_TEN")).isEqualTo(overrideDeadline.toString());
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient).getCaseSummaryFields(caseData.getType());
        verify(infoClient).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository).findByUuid(caseData.getUuid());
    }

    @Test
    public void shouldAuditGetCaseSummary()  {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary caseSummary = caseDataService.getCaseSummary(caseData.getUuid());

        verify(auditClient, times(1)).viewCaseSummaryAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentNull() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(null);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());

    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentException() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        Set<FieldDto> filterFields = new HashSet<>();


        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());

        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseOnlyFilteredAdditionalData() throws ApplicationExceptions.EntityNotFoundException, IOException {

        Set<FieldDto> filterFields = new HashSet<>();

        FieldDto field0 = new FieldDto(UUID.randomUUID(),"TEMPCReference", "what is your TEMPCReference", "Text", new String[]{}, true, true, null);
        filterFields.add(field0);

        FieldDto field1 = new FieldDto(UUID.randomUUID(),"CopyNumberTen",  "what is your CopyNumberTen", "Text", new String[]{},  true, true, null);
        filterFields.add(field1);


        Map<String, String> additionalData = Map.of(
            "TEMPCReference", "test ref",
            "CopyNumberTen", "true",
            "UnfilteredField", "some value"
        );

        CaseData caseData = new CaseData(caseType, caseID, additionalData, objectMapper, deadlineDate);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
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

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCase(caseUUID);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        try {
            caseDataService.getCase(caseUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);

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

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCalculateTotals() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        EntityTotalDto entityTotalDto = new EntityTotalDto(new HashMap(), new HashMap());
        EntityDto<EntityTotalDto> entityDto = new EntityDto<EntityTotalDto>("simpleName", entityTotalDto);
        List<EntityDto<EntityTotalDto>> entityListTotals = new ArrayList();
        entityListTotals.add(entityDto);
        when(infoClient.getEntityListTotals("list")).thenReturn(entityListTotals);

        Map<String, String> totals = caseDataService.calculateTotals(caseData.getUuid(), stageUUID, "list");

        assertThat(totals).isNotNull();
        verify(caseDataRepository, times(2)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateCase() throws JsonProcessingException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditUpdateCase() throws ApplicationExceptions.EntityCreationException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, null);

        verifyZeroInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotUpdateCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.updateCaseData(null, stageUUID, new HashMap<>());
    }

    @Test()
    public void shouldNotUpdateCaseMissingCaseUUID() throws JsonProcessingException {

        try {
            caseDataService.updateCaseData(null, stageUUID, new HashMap<>());
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }


    @Test
    public void shouldUpdateDateReceived() {

        // given
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadlineWarning);

        // when
        caseDataService.updateDateReceived(caseUUID, stageUUID, deadlineDate, 0);

        // then
        assertThat(caseData.getDateReceived()).isEqualTo(deadlineDate);
        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);

        verify(infoClient, times(1)).getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 0);
        verify(infoClient, times(1)).getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);

        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);

    }


    @Test
    public void shouldUpdateDispatchDeadlineDate() {

        // given
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);
        when(infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0)).thenReturn(caseDeadlineWarning);

        // when
        caseDataService.updateDispatchDeadlineDate(caseUUID, stageUUID, deadlineDate);

        // then
        assertThat(caseData.getCaseDeadline()).isEqualTo(deadlineDate);
        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);

        verify(infoClient, times(1)).getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);
        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);

    }


    @Test
    public void shouldUpdateStageDeadline() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 7)).thenReturn(caseDeadline);

        caseDataService.updateStageDeadline(caseData.getUuid(), stageUUID, "TEST", 7);

        verify(caseDataRepository).findByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldCompleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.completeCase(caseData.getUuid(), true);

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
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

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);

        when(caseDataRepository.findAnyByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid(), true);

        verify(caseDataRepository, times(1)).findAnyByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);
        verify(auditClient).deleteAuditLinesForCase(eq(caseData.getUuid()), any(), eq(true));
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditDeleteCase() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , deadlineDate);

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
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenReturn(new CaseDataType("MIN", "a1"));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test
    public void shouldReturnCaseTypeWhenNullReturnedFromInfoClientAndButCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenThrow(RestClientException.class);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(new CaseData(new CaseDataType("", ""),1L, null ));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseTypeByShortCode(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionWhenNullReturnedFromInfoClientAndNoCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseTypeByShortCode(caseTypeShortCode)).thenThrow(RestClientException.class);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCaseType(caseUUID);
    }

    @Test
    public void shouldEvictFromTheCache() {

        caseDataService.clearCachedTemplateForCaseType(caseType.getDisplayName());

        verify(infoClient, times(1)).clearCachedTemplateForCaseType(caseType.getDisplayName());
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void getCaseRef(){

        when(caseDataRepository.getCaseRef(caseUUID)).thenReturn(caseRef);

        String result = caseDataService.getCaseRef(caseUUID);

        assertThat(result).isEqualTo(caseRef);
        verify(caseDataRepository).getCaseRef(caseUUID);
        verifyNoMoreInteractions(infoClient, caseDataRepository, auditClient);

    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.ofEntries(Map.entry("type1", 5),
                Map.entry("type2", 10), Map.entry("type3", 9));

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        LocalDate caseDeadline = LocalDate.now();

        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 5)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 10)).thenReturn(caseDeadline);
        when(infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), 9)).thenReturn(caseDeadline);

        caseDataService.updateDeadlineForStages(caseData.getUuid(), stageUUID, stageTypeAndDaysMap);

        verify(caseDataRepository).findByUuid(caseData.getUuid());
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
        verify(auditClient).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void getCaseDataByReference(){
        String testCaseRef = "TestReference";
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, deadlineDate);
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(caseData);

        CaseData result = caseDataService.getCaseDataByReference(testCaseRef);

        assertThat(result).isEqualTo(caseData);
        verify(caseDataRepository).findByReference(testCaseRef);
        checkNoMoreInteractions();

    }


    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getCaseDataByReference_forNullResult(){
        String testCaseRef = "TestReference";
        when(caseDataRepository.findByReference(testCaseRef)).thenReturn(null);

        caseDataService.getCaseDataByReference(testCaseRef);

    }

    @Test
    public void shouldApplyExtension() {
        final String existingExtensionType = "EXISTING_EXTENSION";
        final String additionalExtensionType = "ADDITIONAL_EXTENSION";

        final CaseDeadlineExtensionType existingExtension =
                new CaseDeadlineExtensionType(existingExtensionType, 10);

        final CaseDeadlineExtensionType additionalExtension =
                new CaseDeadlineExtensionType(additionalExtensionType, 15);

        final CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
        final Set<CaseDeadlineExtensionType> initialDeadlineExtensions = new HashSet<>();
        initialDeadlineExtensions.add(existingExtension);

        caseData.setDeadlineExtensions(initialDeadlineExtensions);

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);
        when(caseDeadlineExtensionTypeRepository.findById(additionalExtensionType))
                .thenReturn(Optional.of(additionalExtension));

        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), caseReceived, 0, 25))
                .thenReturn(caseDeadlineExtended);

        caseDataService.applyExtension(caseUUID, stageUUID, "ADDITIONAL_EXTENSION");

        verify(caseDataRepository).findByUuid(caseUUID);
        verify(infoClient).getCaseDeadline(caseType.getDisplayCode(), caseReceived, 0, 25);
        verify(caseDataRepository).save(caseDataCaptor.capture());
        verify(auditClient).updateCaseAudit(caseData, stageUUID);

        Set<CaseDeadlineExtensionType> deadlineExtensions = caseDataCaptor.getValue().getDeadlineExtensions();

        assertThat(deadlineExtensions.size()).isEqualTo(2);
        assertThat(deadlineExtensions.containsAll(List.of(existingExtension, additionalExtension))).isTrue();

        checkNoMoreInteractions();
    }

    private void checkNoMoreInteractions(){
        verifyNoMoreInteractions(auditClient, caseDataRepository, infoClient);
    }
}
