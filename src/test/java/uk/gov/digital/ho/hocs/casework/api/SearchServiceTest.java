package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("local")
public class SearchServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID transitionNoteUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN", null);
    private final List<CaseDataType> caseDataTypes = List.of(
            CaseDataTypeFactory.from("NXT", "a5", "MIN"), // NXT can be reached through MIN
                caseDataType);


    private SearchService searchService;

    @Mock
    private SearchClient searchClient;
    @Mock
    private InfoClient infoClient;
    @Mock
    private StageService stageService;


    @Before
    public void setUp() {
        this.searchService = new SearchService(stageService, searchClient, infoClient);
    }

    @Test
    public void shouldSearch() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageService.getAllStagesByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = searchService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageService).getAllStagesByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageService);

        assertThat(stageResults).hasSize(1);

    }

    @Test
    public void shouldSearchCaseAndNextCaseTypesPresent() {
        StageWithCaseData stageFound = testCaseWithNextCaseType(Boolean.TRUE);
        assertThat(stageFound.getNextCaseType()).isNotBlank();
    }

    @Test
    public void shouldSearchIncompleteCaseAndNextCaseTypesPresent() {
        StageWithCaseData stageFound = testCaseWithNextCaseType(Boolean.FALSE);
        assertThat(stageFound.getNextCaseType()).isNull();
    }

    private StageWithCaseData testCaseWithNextCaseType(Boolean completeCase) {

        // given
        Set<UUID> caseUUIDS = Set.of(caseUUID);
        StageWithCaseData repositoryStage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        repositoryStage.setCompleted(completeCase);
        repositoryStage.setCaseDataType("MIN");

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageService.getAllStagesByCaseUUIDIn(caseUUIDS)).thenReturn(Set.of(repositoryStage));

        when(infoClient.getAllCaseTypes()).thenReturn(caseDataTypes);

        // when
        Set<StageWithCaseData> stageResults = searchService.search(searchRequest);

        // then
        verify(searchClient).search(searchRequest);
        verify(stageService).getAllStagesByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageService);

        assertThat(stageResults).hasSize(1);

        return stageResults.iterator().next();

    }

    @Test
    public void shouldSearchInactiveStage() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        StageWithCaseData stage_old = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageService.getAllStagesByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = searchService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageService).getAllStagesByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageService);

        assertThat(stageResults).hasSize(1);

    }

    @Test
    public void shouldSearchMultipleStages() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        StageWithCaseData stage_old = new StageWithCaseData(UUID.randomUUID(), "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageService.getAllStagesByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = searchService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageService).getAllStagesByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageService);

        assertThat(stageResults).hasSize(2);

    }

    @Test
    public void shouldSearchNoResults() {

        Set<UUID> caseUUIDS = new HashSet<>(0);

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);

        searchService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verifyNoMoreInteractions(searchClient);
        verifyNoInteractions(stageService);
    }

}
