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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCaseDataRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    StageService stageService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "update_case_data_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, stageService);
    }

    @Test
    public void getCreateCaseNoteRequest() {

        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        UpdateCaseDataRequest updateCaseDataRequest = new UpdateCaseDataRequest(caseUUID, data);

        assertThat(updateCaseDataRequest.getCommand()).isEqualTo(commandName);
        assertThat(updateCaseDataRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(updateCaseDataRequest.getData()).isEqualTo(data);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        doNothing().when(caseDataService).updateCaseData(caseUUID, data);

        UpdateCaseDataRequest updateCaseDataRequest = new UpdateCaseDataRequest(caseUUID, data);

        updateCaseDataRequest.execute(hocsCaseContext);

        verify(caseDataService, times(1)).updateCaseData(caseUUID, data);

        verifyNoMoreInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyZeroInteractions(stageService);

    }

}