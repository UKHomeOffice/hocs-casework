package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    private DeadlineDataService deadlineDataService;


    @Before
    public void setUp() {
        this.deadlineDataService = new DeadlineDataService(deadlineRepository);
    }

    @Test
    public void shouldSaveDeadlines(){
        LocalDate draftDate = LocalDate.now();
        LocalDate finalDate = LocalDate.now().plusDays(1);
        UUID caseUUID = UUID.randomUUID();
        Map<StageType, LocalDate> deadlines = new HashMap<>();
        deadlines.put(StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        deadlines.put(StageType.DCU_MIN_DISPATCH, finalDate);

        when(deadlineRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString())).thenReturn(null);
        when(deadlineRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString())).thenReturn(null);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString());
        verify(deadlineRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString());
        verify(deadlineRepository, times(2)).save(any(StageDeadline.class));

        verifyNoMoreInteractions(deadlineRepository);
    }

    @Test
    public void shouldUpdateDeadlines(){
        LocalDate draftDate = LocalDate.now();
        LocalDate finalDate = LocalDate.now().plusDays(1);
        UUID caseUUID = UUID.randomUUID();
        Map<StageType, LocalDate> deadlines = new HashMap<>();
        deadlines.put(StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        deadlines.put(StageType.DCU_MIN_DISPATCH, finalDate);

        StageDeadline stageDeadline = new StageDeadline(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        StageDeadline stageDeadline1 = new StageDeadline(caseUUID, StageType.DCU_MIN_DISPATCH, finalDate);

        when(deadlineRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString())).thenReturn(stageDeadline);
        when(deadlineRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString())).thenReturn(stageDeadline1);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString());
        verify(deadlineRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString());
        verify(deadlineRepository, times(2)).save(any(StageDeadline.class));

        verifyNoMoreInteractions(deadlineRepository);
    }
}
