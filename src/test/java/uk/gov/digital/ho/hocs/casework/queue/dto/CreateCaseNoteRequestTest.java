package uk.gov.digital.ho.hocs.casework.queue.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.*;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateCaseNoteRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    CorrespondentService correspondentService;

    @Mock
    StageService stageService;

    @Mock
    TopicService topicService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "create_case_note_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCreateCaseNoteRequest() {

        UUID caseUUID = UUID.randomUUID();
        CaseNoteType caseNoteType = CaseNoteType.MANUAL;
        String caseNote = "anyNote";

        CreateCaseNoteRequest createCaseNoteRequest = new CreateCaseNoteRequest(caseUUID, caseNoteType, caseNote);

        assertThat(createCaseNoteRequest.getCommand()).isEqualTo(commandName);
        assertThat(createCaseNoteRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(createCaseNoteRequest.getCaseNoteType()).isEqualTo(caseNoteType);
        assertThat(createCaseNoteRequest.getCaseNote()).isEqualTo(caseNote);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        CaseNoteType caseNoteType = CaseNoteType.MANUAL;
        String caseNote = "anyNote";

        doNothing().when(caseNoteService).createCaseNote(caseUUID, caseNoteType, caseNote);

        CreateCaseNoteRequest createCaseNoteRequest = new CreateCaseNoteRequest(caseUUID, caseNoteType, caseNote);

        createCaseNoteRequest.execute(hocsCaseContext);

        verify(caseNoteService, times(1)).createCaseNote(caseUUID, caseNoteType, caseNote);

        verifyZeroInteractions(caseDataService);
        verifyNoMoreInteractions(caseNoteService);
        verifyZeroInteractions(correspondentService);
        verifyZeroInteractions(stageService);
        verifyZeroInteractions(topicService);

    }

}