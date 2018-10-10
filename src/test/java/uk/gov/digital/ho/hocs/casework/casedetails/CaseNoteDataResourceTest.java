package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteDataResourceTest {

    @Mock
    private CaseNoteDataService caseNoteDataService;

    private CaseNoteDataResource caseNoteDataResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        caseNoteDataResource = new CaseNoteDataResource(caseNoteDataService);
    }

    @Test
    public void shouldGetCase() {

        when(caseNoteDataService.getCaseNote(uuid)).thenReturn(new HashSet<>());

        ResponseEntity<GetCaseNotesResponse> response = caseNoteDataResource.getCaseNotesForCase(uuid);

        verify(caseNoteDataService, times(1)).getCaseNote(uuid);

        verifyNoMoreInteractions(caseNoteDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
