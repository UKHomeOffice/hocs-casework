package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final String caseNoteType = "MANUAL";

    private static final String text = "CASE_NOTE";

    String userId = "any user";

    @Mock
    private CaseNoteRepository caseNoteRepository;

    private CaseNoteService caseNoteService;

    @Mock
    AuditClient auditClient;

    @Mock
    RequestData requestData;

    @Before
    public void setUp() {
        when(requestData.userId()).thenReturn(userId);
        caseNoteService = new CaseNoteService(caseNoteRepository, auditClient, requestData);
    }

    @Test
    public void shouldGetCaseNotes() throws ApplicationExceptions.EntityNotFoundException {

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(
            Set.of(new CaseNote(caseUUID, "MANUAL", text, userId)));

        caseNoteService.getCaseNotes(caseUUID);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test
    public void shouldAuditGetCaseNotes() throws ApplicationExceptions.EntityNotFoundException {
        Set<CaseNote> caseNoteData = Set.of(new CaseNote(caseUUID, "MANUAL", text, userId));

        when(caseNoteRepository.findAllByCaseUUID(caseUUID)).thenReturn(caseNoteData);

        caseNoteService.getCaseNotes(caseUUID);

        verify(auditClient, times(1)).viewCaseNotesAudit(caseUUID);

        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldNotGetCaseNotesMissingUUID() {

        caseNoteService.getCaseNotes(null);

        verify(caseNoteRepository, times(1)).findAllByCaseUUID(null);

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test
    public void shouldGetCaseNote() {

        CaseNote caseNote = new CaseNote(caseUUID, "MANUAL", text, userId);

        when(caseNoteRepository.findByUuid(any(UUID.class))).thenReturn(caseNote);

        caseNoteService.getCaseNote(caseUUID);

        verify(caseNoteRepository, times(1)).findByUuid(any(UUID.class));

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test
    public void shouldAuditGetCaseNote() {
        CaseNote caseNote = new CaseNote(caseUUID, "MANUAL", text, userId);

        when(caseNoteRepository.findByUuid(any(UUID.class))).thenReturn(caseNote);

        caseNoteService.getCaseNote(caseUUID);

        verify(auditClient, times(1)).viewCaseNoteAudit(caseNote);

        verifyNoMoreInteractions(auditClient);
    }

    @Test
    public void shouldCreateCaseNote() throws ApplicationExceptions.EntityCreationException {

        CaseNote caseNote = caseNoteService.createCaseNote(caseUUID, caseNoteType, text);

        assertThat(caseNote.getUuid()).isNotNull();
        verify(caseNoteRepository, times(1)).save(any(CaseNote.class));

        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test
    public void shouldCreateCaseNoteWithAuthor() throws ApplicationExceptions.EntityCreationException {

        CaseNote caseNote = caseNoteService.createCaseNote(caseUUID, caseNoteType, text);

        assertThat(caseNote.getUuid()).isNotNull();
        verify(caseNoteRepository, times(1)).save(any(CaseNote.class));
        verify(requestData, times(1)).userId();
        verifyNoMoreInteractions(caseNoteRepository);
    }

    @Test
    public void shouldAuditCreateCaseNote() throws ApplicationExceptions.EntityCreationException {

        CaseNote caseNote = caseNoteService.createCaseNote(caseUUID, caseNoteType, text);

        assertThat(caseNote.getUuid()).isNotNull();
        verify(auditClient, times(1)).createCaseNoteAudit(caseNote);

        verifyNoMoreInteractions(auditClient);
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

        verifyNoInteractions(caseNoteRepository);

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

        verifyNoInteractions(caseNoteRepository);

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

        verifyNoInteractions(caseNoteRepository);

    }

    @Test
    public void shouldUpdateCaseNote() throws ApplicationExceptions.EntityCreationException {

        CaseNote caseNote = new CaseNote(caseUUID, "MANUAL", text, userId);
        when(caseNoteRepository.findByUuid(caseNote.getUuid())).thenReturn(caseNote);

        CaseNote caseNoteUpdated = caseNoteService.updateCaseNote(caseNote.getUuid(), caseNoteType, text);

        assertThat(caseNoteUpdated.getUuid()).isEqualTo(caseNote.getUuid());
        assertThat(caseNoteUpdated.getEdited()).isNotNull();
        assertThat(caseNoteUpdated.getEditor()).isEqualTo("any user");
        verify(caseNoteRepository).findByUuid(caseNote.getUuid());
        verify(caseNoteRepository).save(any(CaseNote.class));
        verifyNoMoreInteractions(caseNoteRepository);
        verify(requestData).userId();
    }

    @Test
    public void shouldDeleteCaseNote() throws ApplicationExceptions.EntityCreationException {

        CaseNote caseNote = new CaseNote(caseUUID, "MANUAL", text, userId);
        when(caseNoteRepository.findByUuid(caseNote.getUuid())).thenReturn(caseNote);

        CaseNote caseNoteDeleted = caseNoteService.deleteCaseNote(caseNote.getUuid());

        assertThat(caseNoteDeleted.getUuid()).isEqualTo(caseNote.getUuid());
        assertThat(caseNoteDeleted.getDeleted()).isTrue();
        verify(caseNoteRepository).findByUuid(caseNote.getUuid());
        verify(caseNoteRepository).save(any(CaseNote.class));
        verifyNoMoreInteractions(caseNoteRepository);
    }

}
