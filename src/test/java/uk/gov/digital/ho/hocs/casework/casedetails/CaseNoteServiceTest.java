package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseNoteRepository;

import java.time.LocalDateTime;
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

        caseNoteService.createCaseNote(uuid, "CASE NOTE");

        verify(caseNoteRepository, times(1)).save(any());

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {
        caseNoteService.createCaseNote(null, null);
    }


    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        HashSet<CaseNote> caseNoteData = new HashSet<>();
        caseNoteData.add(new CaseNote(1, uuid, uuid, "Case Note", LocalDateTime.now(), false));

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(caseNoteData);

        caseNoteService.getCaseNote(uuid);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        caseNoteService.getCaseNote(uuid);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {
        UUID uuid = UUID.randomUUID();

        when(caseNoteRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        try {
            caseNoteService.getCaseNote(uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws EntityNotFoundException {

        caseNoteService.getCaseNote(null);

    }


}
