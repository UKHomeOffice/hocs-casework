package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.digital.ho.hocs.casework.casedetails.repository.InputDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private InputDataRepository inputDataRepository;

    private InputDataService inputDataService;

    @Mock
    private AuditService auditService;

    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.inputDataService = new InputDataService(inputDataRepository, auditService, new ObjectMapper());

        this.caseDataService = new CaseDataService(
                caseDataRepository,
                inputDataService,
                auditService);
    }

    @Test
    public void shouldCreateCaseWithValidParams() throws EntityCreationException {
        Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(CaseType.MIN);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verify(inputDataRepository, times(1)).save(any(CaseInputData.class));
        verify(auditService, times(1)).writeCreateCaseEvent(caseData);
        verify(auditService, times(1)).writeCreateInputDataEvent(any(CaseInputData.class));

        verifyNoMoreInteractions(caseDataRepository);
        verifyNoMoreInteractions(inputDataRepository);
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
        verifyZeroInteractions(inputDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {
        Long caseID = 12345L;
        CaseData caseData = new CaseData(CaseType.MIN, caseID);
        CaseInputData caseInputData = new CaseInputData(caseData.getUuid());

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);
        when(inputDataRepository.findByCaseUUID(caseData.getUuid())).thenReturn(caseInputData);


        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(inputDataRepository, times(1)).findByCaseUUID(caseData.getUuid());
        verify(auditService, times(1)).writeGetCaseEvent(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);
        verifyNoMoreInteractions(inputDataRepository);
        verifyNoMoreInteractions(auditService);
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
        verifyZeroInteractions(inputDataRepository);
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
        verifyZeroInteractions(inputDataRepository);
        verifyZeroInteractions(auditService);
    }
}
