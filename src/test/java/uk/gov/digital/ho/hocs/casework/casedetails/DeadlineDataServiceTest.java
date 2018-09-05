package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeadlineDataServiceTest {

    @Mock
    private DeadlineDataRepository deadlineDataRepository;

    private DeadlineDataService deadlineDataService;


    @Before
    public void setUp() { this.deadlineDataService = new DeadlineDataService(deadlineDataRepository);}

    @Test
    public void shouldSaveDeadlines(){
        LocalDate draftDate = LocalDate.now();
        LocalDate finalDate = LocalDate.now().plusDays(1);
        UUID caseUUID = UUID.randomUUID();
        Map<StageType, LocalDate> deadlines = new HashMap<>();
        deadlines.put(StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        deadlines.put(StageType.DCU_MIN_DISPATCH, finalDate);

        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString())).thenReturn(null);
        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString())).thenReturn(null);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString());
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString());
        verify(deadlineDataRepository, times(2)).save(any(DeadlineData.class));

        verifyNoMoreInteractions(deadlineDataRepository);
    }

    @Test
    public void shouldUpdateDeadlines(){
        LocalDate draftDate = LocalDate.now();
        LocalDate finalDate = LocalDate.now().plusDays(1);
        UUID caseUUID = UUID.randomUUID();
        Map<StageType, LocalDate> deadlines = new HashMap<>();
        deadlines.put(StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        deadlines.put(StageType.DCU_MIN_DISPATCH, finalDate);

        DeadlineData deadlineData = new DeadlineData(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT, draftDate);
        DeadlineData deadlineData1 = new DeadlineData(caseUUID, StageType.DCU_MIN_DISPATCH, finalDate);

        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString())).thenReturn(deadlineData);
        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString())).thenReturn(deadlineData1);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_INITIAL_DRAFT.toString());
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID, StageType.DCU_MIN_DISPATCH.toString());
        verify(deadlineDataRepository, times(2)).save(any(DeadlineData.class));

        verifyNoMoreInteractions(deadlineDataRepository);
    }
}
