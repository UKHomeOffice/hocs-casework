package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNoteResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNotesResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteResourceTest {

    private final UUID caseUUID = UUID.randomUUID();
    @Mock
    private CaseNoteService caseNoteService;
    private CaseNoteResource caseNoteResource;

    @Before
    public void setUp() {
        caseNoteResource = new CaseNoteResource(caseNoteService);
    }

    @Test
    public void shouldGetCaseNotes() {

        when(caseNoteService.getCaseNotes(caseUUID)).thenReturn(new HashSet<>());

        ResponseEntity<GetCaseNotesResponse> response = caseNoteResource.getCaseNotesForCase(caseUUID);

        verify(caseNoteService, times(1)).getCaseNotes(caseUUID);

        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseNote() {
        UUID noteUUID = UUID.randomUUID();

        when(caseNoteService.getCaseNote(noteUUID)).thenReturn(new CaseNote(caseUUID,"","", ""));

        ResponseEntity<GetCaseNoteResponse> response = caseNoteResource.getCaseNote(caseUUID, noteUUID);

        verify(caseNoteService, times(1)).getCaseNote(noteUUID);
        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldCreateCaseNote() {
        when(caseNoteService.createCaseNote(caseUUID, "TYPE", "case note", "STAGETYPE")).thenReturn(new CaseNote(caseUUID,"TYPE","case note", "STAGETYPE"));

        ResponseEntity<CreateCaseNoteResponse> response = caseNoteResource.createCaseNote(caseUUID, new CreateCaseNoteRequest("TYPE","case note", "STAGETYPE"));

        verify(caseNoteService, times(1)).createCaseNote(caseUUID, "TYPE", "case note", "STAGETYPE");
        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
