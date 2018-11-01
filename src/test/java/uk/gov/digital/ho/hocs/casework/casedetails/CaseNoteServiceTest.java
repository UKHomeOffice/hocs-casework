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

    private final UUID caseUUID = UUID.randomUUID();
    private final CaseNoteType caseNoteType = CaseNoteType.MANUAL;
    private final String text = "CASE_NOTE";

    @Before
    public void setUp() {
        this.caseNoteService = new CaseNoteService(caseNoteRepository);
    }

    @Test
    public void shouldGetCase() throws EntityNotFoundException {
        HashSet<CaseNote> caseNoteData = new HashSet<>();
        caseNoteData.add(new CaseNote(caseUUID, CaseNoteType.MANUAL, "Case Note"));

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(caseNoteData);

        caseNoteService.getCaseNotes(caseUUID);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(null);

        caseNoteService.getCaseNotes(caseUUID);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(null);

        try {
            caseNoteService.getCaseNotes(caseUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws EntityNotFoundException {

        caseNoteService.getCaseNotes(null);

    }

    @Test
    public void shouldNotGetCaseMissingUUID() {

        try {
            caseNoteService.getCaseNotes(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test
    public void shouldCreateCaseNote() throws EntityCreationException {

        caseNoteService.createCaseNote(caseUUID, caseNoteType, text);

        verify(caseNoteRepository, times(1)).save(any(CaseNote.class));

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {
        caseNoteService.createCaseNote(null, null, null);
    }

}
