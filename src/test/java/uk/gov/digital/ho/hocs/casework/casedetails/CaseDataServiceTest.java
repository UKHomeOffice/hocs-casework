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
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CaseInputDataRepository caseInputDataRepository;

    @Mock
    private AuditService auditService;

    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(
                caseDataRepository,
                caseInputDataRepository,
                auditService);
    }

    @Test
    public void shouldCreateCaseWithValidParams() throws EntityCreationException {
        Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        caseDataService.createCase(CaseType.MIN);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(any(CaseData.class));
        verify(caseInputDataRepository, times(1)).save(any(CaseInputData.class));
        verify(auditService, times(1)).writeCreateCaseEvent(any(CaseData.class), any(CaseInputData.class));

        verifyNoMoreInteractions(caseDataRepository);
        verifyNoMoreInteractions(caseInputDataRepository);
        verifyNoMoreInteractions(auditService);

    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingUUIDException() throws EntityCreationException {
        caseDataService.createCase(null);
    }

    @Test()
    public void shouldNotCreateCaseMissingUUID() {
        try {
            caseDataService.createCase(null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(caseInputDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        CaseData caseData = new CaseData();


        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseInputDataRepository, times(1)).findByCaseUUID(caseData.getUuid());
        verify(auditService, times(1)).writeGetCaseEvent(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);
        verifyNoMoreInteractions(caseInputDataRepository);
        verifyNoMoreInteractions(auditService);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {
        UUID uuid = UUID.randomUUID();

        when(caseDataRepository.findByUuid(uuid)).thenReturn(null);

        caseDataService.getCase(uuid);
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
        verify(auditService, times(1)).writeGetCaseEvent(uuid);

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(caseInputDataRepository);
        verifyNoMoreInteractions(auditService);
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
        verify(auditService, times(1)).writeGetCaseEvent(null);

        verifyNoMoreInteractions(caseDataRepository);
        verifyZeroInteractions(caseInputDataRepository);
        verifyNoMoreInteractions(auditService);
    }
}
