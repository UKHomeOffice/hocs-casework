package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

    @Mock
    private CaseNoteRepository caseNoteRepository;

    private CaseNoteService caseNoteService;

    private UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.caseNoteService = new CaseNoteService(caseNoteRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParams() throws EntityCreationException {

        caseNoteService.createCaseNote(uuid, CaseNoteType.MANUAL, "CASE NOTE");

        verify(caseNoteRepository, times(1)).save(any());

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {
        caseNoteService.createCaseNote(null, null, null);
    }


    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        HashSet<CaseNote> caseNoteData = new HashSet<>();
        caseNoteData.add(new CaseNote(uuid, CaseNoteType.MANUAL, "Case Note"));

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(caseNoteData);

        caseNoteService.getCaseNotesForCase(uuid);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        caseNoteService.getCaseNotesForCase(uuid);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {
        UUID uuid = UUID.randomUUID();

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        try {
            caseNoteService.getCaseNotesForCase(uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws EntityNotFoundException {

        caseNoteService.getCaseNotesForCase(null);

    }


}
