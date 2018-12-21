package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;

import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    private static final long caseID = 12345L;

    private final CaseDataType caseType = new CaseDataType("MIN", "a1");

    private final UUID caseUUID = UUID.randomUUID();

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private InfoClient infoClient;
    private CaseDataService caseDataService;

    @Mock
    private AuditClient auditClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private LocalDate caseDeadline = LocalDate.now().plusDays(20);

    private LocalDate caseReceived = LocalDate.now();

    private UUID primaryCorrespondentUUID = UUID.randomUUID();

    private UUID stageUUID = UUID.randomUUID();

    @Mock
    private StageService stageService;
    @Mock
    private CorrespondentService correspondentService;


    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(caseDataRepository, infoClient, objectMapper, correspondentService, stageService, auditClient);
    }

    @Test
    public void shouldCreateCase() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(caseType, new HashMap<>(), caseDeadline, caseReceived);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws ApplicationExceptions.EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(caseType, null, caseDeadline, caseReceived);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }
//
//    @Test
//    public void shouldAuditCreateCase() throws ApplicationExceptions.EntityCreationException {
//
//        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
//
//        CaseData caseData = caseDataService.createCase(caseType, new HashMap<>(), caseDeadline, caseReceived);
//
//
//        verify(auditclient, times(1)).createCaseAudit(caseData);
//
//        verifyNoMoreInteractions(auditclient);
//    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.createCase(null, new HashMap<>(),caseDeadline, caseReceived);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null, new HashMap<>(),caseDeadline, caseReceived);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParams() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper,caseDeadline, caseReceived);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        Set<String> filterFields =new HashSet<String>(){{ add("TEMPCReference"); }};

        Set<Stage> activeStages = new HashSet<Stage>(){{
            add(new Stage(UUID.randomUUID(), "DCU_DTEN_COPY_NUMBER_TEN", UUID.randomUUID(), LocalDate.now()));
        }};

        Map<String, LocalDate> deadlines = new HashMap<String, LocalDate>() {{
            put("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10));
            put("DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        }};
        Correspondent correspondent = new Correspondent(caseData.getUuid(), "CORRESPONDENT", "some name,",
                new Address("","","","",""), "12345","some email", "some ref");



        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);
        when(stageService.getActiveStagesByCaseUUID(caseData.getUuid())).thenReturn(activeStages);
        when(correspondentService.getCorrespondent(caseData.getUuid(), primaryCorrespondentUUID)).thenReturn(correspondent);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getActiveStages().size()).isEqualTo(1);


        verify(stageService, times(1)).getActiveStagesByCaseUUID(caseData.getUuid());
        verify(correspondentService, times(1)).getCorrespondent(caseData.getUuid(), primaryCorrespondentUUID);
        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentNull() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline, caseReceived);
        caseData.setPrimaryCorrespondentUUID(null);
        Set<String> filterFields = new HashSet<String>() {{
            add("TEMPCReference");
        }};

        Set<Stage> activeStages = new HashSet<Stage>() {{
            add(new Stage(UUID.randomUUID(), "DCU_DTEN_COPY_NUMBER_TEN", UUID.randomUUID(), LocalDate.now()));
        }};

        Map<String, LocalDate> deadlines = new HashMap<String, LocalDate>() {{
            put("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10));
            put("DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        }};

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);
        when(stageService.getActiveStagesByCaseUUID(caseData.getUuid())).thenReturn(activeStages);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getActiveStages().size()).isEqualTo(1);


        verify(stageService, times(1)).getActiveStagesByCaseUUID(caseData.getUuid());
        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());

        verifyZeroInteractions(correspondentService);
    }

    @Test
    public void shouldGetCaseSummaryWithValidParamsPrimaryCorrespondentException() throws ApplicationExceptions.EntityNotFoundException, IOException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline, caseReceived);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        Set<String> filterFields = new HashSet<String>() {{
            add("TEMPCReference");
        }};

        Set<Stage> activeStages = new HashSet<Stage>() {{
            add(new Stage(UUID.randomUUID(), "DCU_DTEN_COPY_NUMBER_TEN", UUID.randomUUID(), LocalDate.now()));
        }};

        Map<String, LocalDate> deadlines = new HashMap<String, LocalDate>() {{
            put("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10));
            put("DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        }};

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);
        when(stageService.getActiveStagesByCaseUUID(caseData.getUuid())).thenReturn(activeStages);

        when(correspondentService.getCorrespondent(caseData.getUuid(), caseData.getPrimaryCorrespondentUUID())).thenThrow(ApplicationExceptions.EntityNotFoundException.class);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getActiveStages().size()).isEqualTo(1);


        verify(stageService, times(1)).getActiveStagesByCaseUUID(caseData.getUuid());
        verify(infoClient, times(1)).getCaseSummaryFields(caseData.getType());
        verify(infoClient, times(1)).getDeadlines(caseData.getType(), caseData.getDateReceived());
        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
    }

    @Test
    public void shouldGetCaseOnlyFilteredAdditionalData() throws ApplicationExceptions.EntityNotFoundException, IOException {

        Set<String> filterFields =new HashSet<String>(){{
            add("TEMPCReference");
            add("CopyNumberTen");
        }};

        Set<Stage> activeStages = new HashSet<Stage>(){{
            add(new Stage(UUID.randomUUID(), "DCU_DTEN_COPY_NUMBER_TEN", UUID.randomUUID(), LocalDate.now()));
        }};

        Map<String, String> additionalData = new HashMap<String, String>(){{
            put("TEMPCReference", "test ref");
            put("CopyNumberTen", "true");
            put("UnfilteredField", "some value");
        }};

        CaseData caseData = new CaseData(caseType, caseID, additionalData, objectMapper,caseDeadline, caseReceived);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);

        Map<String, LocalDate> deadlines = new HashMap<String, LocalDate>() {{
            put("DCU_DTEN_COPY_NUMBER_TEN", LocalDate.now().plusDays(10));
            put("DCU_DTEN_DATA_INPUT", LocalDate.now().plusDays(20));
        }};

        Correspondent correspondent = new Correspondent(caseData.getUuid(), "CORRESPONDENT", "some name,",
                new Address("","","","",""), "12345","some email", "some ref");

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getCaseSummaryFields(caseData.getType())).thenReturn(filterFields);
        when(infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived())).thenReturn(deadlines);
        when(stageService.getActiveStagesByCaseUUID(caseData.getUuid())).thenReturn(activeStages);
        when(correspondentService.getCorrespondent(caseData.getUuid(), primaryCorrespondentUUID)).thenReturn(correspondent);

        CaseSummary result = caseDataService.getCaseSummary(caseData.getUuid());

        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getStageDeadlines()).isEqualTo(deadlines);
        assertThat(result.getCaseDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(result.getAdditionalFields().get("TEMPCReference")).isEqualTo("test ref");
        assertThat(result.getAdditionalFields().get("CopyNumberTen")).isEqualTo("true");
        assertThat(result.getAdditionalFields()).doesNotContainKey("UnfilteredField");
    }

    @Test
    public void shouldGetCaseWithValidParams() throws ApplicationExceptions.EntityNotFoundException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper,caseDeadline, caseReceived);

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
    public void shouldUpdateCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline, caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, new HashMap<>());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline, caseReceived);

        caseDataService.updateCaseData(caseData.getUuid(), stageUUID, null);

        verifyZeroInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotUpdateCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.updateCaseData(null, stageUUID, new HashMap<>());
    }

    @Test()
    public void shouldNotUpdateCaseMissingCaseUUID() {

        try {
            caseDataService.updateCaseData(null, stageUUID, new HashMap<>());
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdatePriorityCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline , caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updatePriority(caseData.getUuid(), false);

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotUpdatePriorityCaseMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {

        caseDataService.updatePriority(null, false);
    }

    @Test()
    public void shouldNotUpdatePriorityCaseMissingCaseUUID() {

        try {
            caseDataService.updatePriority(null, false);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper, caseDeadline , caseReceived);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.deleteCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
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
        when(infoClient.getCaseType(caseTypeShortCode)).thenReturn(new CaseDataType("MIN", "a1"));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseType(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test
    public void shouldReturnCaseTypeWhenNullReturnedFromInfoClientAndButCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseType(caseTypeShortCode)).thenThrow(ApplicationExceptions.ResourceServerException.class);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(new CaseData(new CaseDataType("", ""),1L, null, null ));

        caseDataService.getCaseType(caseUUID);

        verify(infoClient, times(1)).getCaseType(caseTypeShortCode);
        verifyNoMoreInteractions(infoClient);
        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionWhenNullReturnedFromInfoClientAndNoCaseInCaseDataOnGetCaseType() {
        String caseTypeShortCode = caseUUID.toString().substring(34);
        when(infoClient.getCaseType(caseTypeShortCode)).thenThrow(ApplicationExceptions.ResourceServerException.class);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCaseType(caseUUID);
    }
}
