package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final CaseNoteType caseNoteType = CaseNoteType.MANUAL;
    private static final String text = "CASE_NOTE";
    @Mock
    private CaseNoteRepository caseNoteRepository;
    private CaseNoteService caseNoteService;

    @Before
    public void setUp() {
        caseNoteService = new CaseNoteService(caseNoteRepository);
    }

    @Test
    public void shouldGetCaseNotes() throws ApplicationExceptions.EntityNotFoundException {
        HashSet<CaseNote> caseNoteData = new HashSet<>();
        caseNoteData.add(new CaseNote(caseUUID, CaseNoteType.MANUAL, text));

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(caseNoteData);

        caseNoteService.getCaseNotes(caseUUID);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCaseNotesWithValidParamsNotFoundException() {

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(null);

        caseNoteService.getCaseNotes(caseUUID);
    }

    @Test
    public void shouldNotGetCaseNotesNotFound() {

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(null);

        try {
            caseNoteService.getCaseNotes(caseUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test
    public void shouldNotGetCaseNotesMissingUUID() {

        caseNoteService.getCaseNotes(null);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(null);

        verifyNoMoreInteractions(caseNoteRepository);

    }

    @Test
    public void shouldCreateCaseNote() throws ApplicationExceptions.EntityCreationException {

        caseNoteService.createCaseNote(caseUUID, caseNoteType, text);

        verify(caseNoteRepository, times(1)).save(any(CaseNote.class));

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseNoteMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException {
        caseNoteService.createCaseNote(null, caseNoteType, text);
    }

    @Test
    public void shouldNotCreateCaseNoteMissingCaseUUID() {

        try {
            caseNoteService.createCaseNote(null, caseNoteType, text);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(caseNoteRepository);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseNoteMissingCaseNoteTypeException() throws ApplicationExceptions.EntityCreationException {
        caseNoteService.createCaseNote(caseUUID, null, text);
    }

    @Test
    public void shouldNotCreateCaseNoteMissingCaseNoteType() {

        try {
            caseNoteService.createCaseNote(caseUUID, null, text);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(caseNoteRepository);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseNoteMissingTextException() throws ApplicationExceptions.EntityCreationException {
        caseNoteService.createCaseNote(caseUUID, caseNoteType, null);
    }

    @Test
    public void shouldNotCreateCaseNoteMissingText() {

        try {
            caseNoteService.createCaseNote(caseUUID, caseNoteType, null);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(caseNoteRepository);

    }

}
