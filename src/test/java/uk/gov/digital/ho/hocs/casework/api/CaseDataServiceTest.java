package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.ConstituencyDto;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

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
    private static final String OFFLINE_QA_USER = "OfflineQaUser";
    
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
    private final UUID caseUUID = UUID.randomUUID();

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private AuditClient auditClient;

    private CaseDataService caseDataService;

    private ObjectMapper objectMapper;

    private LocalDate caseDeadline = LocalDate.now().plusDays(20);

    private LocalDate caseReceived = LocalDate.now();

    private UUID primaryCorrespondentUUID = UUID.randomUUID();

    private UUID stageUUID = UUID.randomUUID();

    private SpringConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new SpringConfiguration();
        objectMapper = configuration.initialiseObjectMapper();
        this.caseDataService = new CaseDataService(caseDataRepository, infoClient, objectMapper, auditClient);
    }

    @Test
    public void shouldCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), caseReceived)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), caseReceived);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), caseReceived);
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), caseReceived)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayName(), null, caseReceived);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(infoClient, times(1)).getCaseDeadline(caseType.getDisplayCode(), caseReceived);
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(infoClient.getCaseDeadline(caseType.getDisplayCode(), caseReceived)).thenReturn(caseDeadline);
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);

        CaseData caseData = caseDataService.createCase(caseType.getDisplayCode(), new HashMap<>(), caseReceived);

        verify(auditClient, times(1)).createCaseAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.createCase(null, new HashMap<>(), caseReceived);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null, new HashMap<>(), caseReceived);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }


    @Test
    public void shouldGetCaseTimeline()  {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
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
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
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
    public void shouldGetCaseTeams() throws JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
        UUID auditResponseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.of(2019,1,1);

        String payload = objectMapper.writeValueAsString(new AuditPayload.StageAllocation(stageUUID, teamUUID, "STAGE_TYPE", deadline));

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

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
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
    public void shouldAuditGetCaseSummary()  {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
        Set<FieldDto> filterFields = new HashSet<>();

        Map<String, LocalDate> deadlines = Map.of(
                "DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10),
                "DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);

        CaseSummary caseSummary = caseDataService.getCaseSummary(caseData.getUuid());

        verify(auditClient, times(1)).viewCaseSummaryAudit(caseData, caseSummary);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentNull() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
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

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);
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

        FieldDto field0 = new FieldDto(UUID.randomUUID(),"TEMPCReference", "what is your TEMPCReference", "Text", new String[]{}, true, true);
        filterFields.add(field0);

        FieldDto field1 = new FieldDto(UUID.randomUUID(),"CopyNumberTen",  "what is your CopyNumberTen", "Text", new String[]{},  true, true);
        filterFields.add(field1);


        Map<String, String> additionalData = Map.of(
            "TEMPCReference", "test ref",
            "CopyNumberTen", "true",
            "UnfilteredField", "some value"
        );

        CaseData caseData = new CaseData(caseType, caseID, additionalData, objectMapper, caseReceived);
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
    public void shouldGetRegionUUIDForCase() {

        Correspondent correspondent = new Correspondent(UUID.randomUUID(), "TYPE", "name",
                new Address("postcode","address1","address2","address3","county"),
                "phone", "email", "", "extKey");
        CaseData caseData = mock(CaseData.class);
        when(caseData.getPrimaryCorrespondent()).thenReturn(correspondent);
        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        ConstituencyDto constituencyDto = mock(ConstituencyDto.class);
        when(constituencyDto.getRegionUUID()).thenReturn(UUID.fromString("85462836-4de7-4297-b60f-2bd260b3a686"));
        when(infoClient.getConstituencyByMemberExternalKey("extKey")).thenReturn(constituencyDto);

        UUID regionUUID = caseDataService.getRegionUUIDForCase(caseData.getUuid());

        verify(caseDataRepository).findByUuid(caseData.getUuid());
        verifyNoMoreInteractions(caseDataRepository);
        assertThat(regionUUID).isEqualTo(UUID.fromString("85462836-4de7-4297-b60f-2bd260b3a686"));
    }

    @Test
    public void shouldGetCaseWithValidParams() throws ApplicationExceptions.EntityNotFoundException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);

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
    public void shouldUpdateCase() throws JsonProcessingException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditUpdateCase() throws ApplicationExceptions.EntityCreationException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(auditClient, times(1)).updateCaseAudit(caseData, stageUUID);
        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseReceived);

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
    public void shouldCompleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , caseReceived);

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

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldAuditDeleteCase() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid());

        verify(auditClient, times(1)).deleteCaseAudit(caseData);
        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.deleteCase(null);
    }

    @Test()
    public void shouldNotDeleteCaseMissingCaseUUID() {

        try {
            caseDataService.deleteCase(null);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

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
    public void shouldGetCaseDateReceived() {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper , caseReceived);

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);

        caseDataService.getCaseDateReceived(caseUUID);

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseDataRepository);

    }
}
