package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseNoteDataRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteDataServiceTest {

    @Mock
    private CaseNoteDataRepository caseNoteDataRepository;

    private CaseNoteDataService caseNoteDataService;

    private UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.caseNoteDataService = new CaseNoteDataService(caseNoteDataRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParams() throws EntityCreationException {

        caseNoteDataService.createCaseNote(uuid,"CASE NOTE");

        verify(caseNoteDataRepository, times(1)).save(any());

        verifyNoMoreInteractions(caseNoteDataRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {
        caseNoteDataService.createCaseNote(null, null);
    }


    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        HashSet<CaseNoteData> caseNoteData = new HashSet<>();
        caseNoteData.add( new CaseNoteData(1, uuid,uuid,"Case Note", LocalDateTime.now(),false));

        when(caseNoteDataRepository.findAllByCaseUUID(uuid)).thenReturn(caseNoteData);

        caseNoteDataService.getCaseNote(uuid);

        verify(caseNoteDataRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteDataRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseNoteDataRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        caseNoteDataService.getCaseNote(uuid);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {
        UUID uuid = UUID.randomUUID();

        when(caseNoteDataRepository.findAllByCaseUUID(uuid)).thenReturn(null);

        try {
            caseNoteDataService.getCaseNote(uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseNoteDataRepository, times(1)).findAllByCaseUUID(uuid);

        verifyNoMoreInteractions(caseNoteDataRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws EntityNotFoundException {

        caseNoteDataService.getCaseNote(null);

    }


}
