package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.DeadlineDataDto;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        DeadlineDataDto deadline = new DeadlineDataDto("draft",draftDate);
        DeadlineDataDto deadline1 = new DeadlineDataDto("final",finalDate);
        Set<DeadlineDataDto> deadlines = new HashSet<>();
        deadlines.add(deadline);
        deadlines.add(deadline1);

        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline.getStage())).thenReturn(null);
        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline1.getStage())).thenReturn(null);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID,deadline.getStage());
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID,deadline1.getStage());
        verify(deadlineDataRepository, times(2)).save(any(DeadlineData.class));

        verifyNoMoreInteractions(deadlineDataRepository);
    }

    @Test
    public void shouldUpdateDeadlines(){
        LocalDate draftDate = LocalDate.now();
        LocalDate finalDate = LocalDate.now().plusDays(1);
        UUID caseUUID = UUID.randomUUID();
        DeadlineDataDto deadline = new DeadlineDataDto("draft",draftDate);
        DeadlineDataDto deadline1 = new DeadlineDataDto("final",finalDate);
        Set<DeadlineDataDto> deadlines = new HashSet<>();
        deadlines.add(deadline);
        deadlines.add(deadline1);
        DeadlineData deadlineData = new DeadlineData(caseUUID,draftDate,"draft");
        DeadlineData deadlineData1 = new DeadlineData(caseUUID,finalDate,"final");

        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline.getStage())).thenReturn(deadlineData);
        when(deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline1.getStage())).thenReturn(deadlineData1);

        deadlineDataService.updateDeadlines(caseUUID, deadlines);
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID,deadline.getStage());
        verify(deadlineDataRepository, times(1)).findByCaseUUIDAndStage(caseUUID,deadline1.getStage());
        verify(deadlineDataRepository, times(2)).save(any(DeadlineData.class));

        verifyNoMoreInteractions(deadlineDataRepository);
    }
}
