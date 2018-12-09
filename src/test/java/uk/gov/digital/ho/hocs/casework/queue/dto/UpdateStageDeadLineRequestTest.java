package uk.gov.digital.ho.hocs.casework.queue.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStageDeadLineRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    StageService stageService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "update_stage_deadline_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, stageService);
    }

    @Test
    public void getCompleteStageRequest() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        UpdateStageDeadlineRequest updateStageDeadlineRequest = new UpdateStageDeadlineRequest(caseUUID, stageUUID, deadline);

        assertThat(updateStageDeadlineRequest.getCommand()).isEqualTo(commandName);
        assertThat(updateStageDeadlineRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(updateStageDeadlineRequest.getStageUUID()).isEqualTo(stageUUID);
        assertThat(updateStageDeadlineRequest.getDeadline()).isEqualTo(deadline);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        UpdateStageDeadlineRequest updateStageDeadlineRequest = new UpdateStageDeadlineRequest(caseUUID, stageUUID, deadline);

        updateStageDeadlineRequest.execute(hocsCaseContext);

        verify(stageService, times(1)).updateDeadline(caseUUID, stageUUID, deadline);

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyNoMoreInteractions(stageService);

    }
}