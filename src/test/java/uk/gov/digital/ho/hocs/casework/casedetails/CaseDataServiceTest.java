package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private CaseDataRepository caseDataRepository;


    @Mock
    private AuditService auditService;

    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(
                caseDataRepository,
                auditService);
    }

    @Test
    public void shouldCreateCaseWithValidParams() throws EntityCreationException {
        Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(CaseType.MIN);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verify(auditService, times(1)).createCaseEvent(caseData);

        verifyNoMoreInteractions(caseDataRepository);
        verifyNoMoreInteractions(auditService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {
        caseDataService.createCase(null);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {
        Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(auditService);

    }

    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        Long caseID = 12345L;
        CaseData caseData = new CaseData(CaseType.MIN, caseID);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(auditService);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(null);

        caseDataService.getCase(caseData.getUuid());
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {
        UUID uuid = UUID.randomUUID();

        when(caseDataRepository.findByUuid(uuid)).thenReturn(null);

        try {
            caseDataService.getCase(uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(uuid);

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(auditService);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseMissingUUIDException() throws EntityNotFoundException {

        caseDataService.getCase(null);

    }

    @Test
    public void shouldNotGetCaseMissingUUID() {

        try {
            caseDataService.getCase(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(auditService);
    }


}
