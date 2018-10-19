package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseNotesResponse;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteResourceTest {

    @Mock
    private CaseNoteService caseNoteService;

    private CaseNoteResource caseNoteResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        caseNoteResource = new CaseNoteResource(caseNoteService);
    }

    @Test
    public void shouldGetCase() {

        when(caseNoteService.getCaseNote(uuid)).thenReturn(new HashSet<>());

        ResponseEntity<GetCaseNotesResponse> response = caseNoteResource.getCaseNotesForCase(uuid);

        verify(caseNoteService, times(1)).getCaseNote(uuid);

        verifyNoMoreInteractions(caseNoteService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
