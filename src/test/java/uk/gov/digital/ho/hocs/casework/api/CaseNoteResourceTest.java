package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNoteResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNotesResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

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

        when(caseNoteService.getCaseNote(noteUUID)).thenReturn(new CaseNote(1L,noteUUID,LocalDateTime.now(), "",caseUUID,"", "a user", false, null, null));

        ResponseEntity<GetCaseNoteResponse> response = caseNoteResource.getCaseNote(caseUUID, noteUUID);

        verify(caseNoteService, times(1)).getCaseNote(noteUUID);
        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateCaseNote() {
        UUID noteUUID = UUID.randomUUID();

        when(caseNoteService.createCaseNote(caseUUID, "TYPE", "case note")).thenReturn(new CaseNote(1L,noteUUID,LocalDateTime.now(), "TYPE",caseUUID,"case note", "a user", false, null, null));

        ResponseEntity<UUID> response = caseNoteResource.createCaseNote(caseUUID, new CreateCaseNoteRequest("TYPE","case note"));

        verify(caseNoteService, times(1)).createCaseNote(caseUUID, "TYPE", "case note");
        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(noteUUID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateCaseNote() {
        UUID noteUUID = UUID.randomUUID();
        when(caseNoteService.updateCaseNote(noteUUID, "TYPE", "case note")).thenReturn(new CaseNote(1L,noteUUID,LocalDateTime.now(), "TYPE",caseUUID,"case note", "a user", false, null, null));

        ResponseEntity<GetCaseNoteResponse> response = caseNoteResource.updateCaseNote(caseUUID, noteUUID, new CreateCaseNoteRequest("TYPE","case note"));

        verify(caseNoteService, times(1)).updateCaseNote(noteUUID, "TYPE", "case note");
        verifyNoMoreInteractions(caseNoteService);
        assertThat(response).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(noteUUID);
        assertThat(response.getBody().getCaseUUID()).isEqualTo(caseUUID);
        assertThat(response.getBody().getType()).isEqualTo("TYPE");
        assertThat(response.getBody().getText()).isEqualTo("case note");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCaseNote() {
        UUID noteUUID = UUID.randomUUID();
        when(caseNoteService.deleteCaseNote(noteUUID)).thenReturn(new CaseNote(1L,noteUUID,LocalDateTime.now(), "TYPE",caseUUID,"case note", "a user", false, null, null));

        ResponseEntity<UUID> response = caseNoteResource.deleteCaseNote(caseUUID, noteUUID);

        verify(caseNoteService, times(1)).deleteCaseNote(noteUUID);
        verifyNoMoreInteractions(caseNoteService);
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(noteUUID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
