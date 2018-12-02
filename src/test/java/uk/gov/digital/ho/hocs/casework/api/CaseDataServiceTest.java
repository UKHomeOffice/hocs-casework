package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    private static final long caseID = 12345L;
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
    private final UUID caseUUID = UUID.randomUUID();
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private InfoClient infoClient;
    private CaseDataService caseDataService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(caseDataRepository, infoClient, objectMapper);
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(caseType, new HashMap<>());

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateCaseWithValidParamsNullData() throws EntityCreationException {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(caseType, null);

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws EntityCreationException {

        caseDataService.createCase(null, new HashMap<>());
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            caseDataService.createCase(null, new HashMap<>());
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldGetCaseWithValidParams() throws EntityNotFoundException {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.getCase(caseData.getUuid());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());

        verifyNoMoreInteractions(caseDataRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCaseWithValidParamsNotFoundException() {

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        caseDataService.getCase(caseUUID);
    }

    @Test
    public void shouldNotGetCaseWithValidParamsNotFound() {

        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(null);

        try {
            caseDataService.getCase(caseUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);

        verifyNoMoreInteractions(caseDataRepository);
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
    }

    @Test
    public void shouldUpdateCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updateCaseData(caseData.getUuid(), new HashMap<>());

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateCaseNullData() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper);

        caseDataService.updateCaseData(caseData.getUuid(), null);

        verifyZeroInteractions(caseDataRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotUpdateCaseMissingCaseUUIDException() throws EntityCreationException {

        caseDataService.updateCaseData(null, new HashMap<>());
    }

    @Test()
    public void shouldNotUpdateCaseMissingCaseUUID() {

        try {
            caseDataService.updateCaseData(null, new HashMap<>());
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdatePriorityCase() {

        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), objectMapper);

        when(caseDataRepository.findByUuid(caseData.getUuid())).thenReturn(caseData);

        caseDataService.updatePriority(caseData.getUuid(), false);

        verify(caseDataRepository, times(1)).findByUuid(caseData.getUuid());
        verify(caseDataRepository, times(1)).save(caseData);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotUpdatePriorityCaseMissingCaseUUIDException() throws EntityCreationException {

        caseDataService.updatePriority(null, false);
    }

    @Test()
    public void shouldNotUpdatePriorityCaseMissingCaseUUID() {

        try {
            caseDataService.updatePriority(null, false);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCase() {

        caseDataService.deleteCase(caseUUID);

        verify(caseDataRepository, times(1)).deleteCase(caseUUID);

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCaseNull() {

        caseDataService.deleteCase(null);

        verify(caseDataRepository, times(1)).deleteCase(null);

        verifyNoMoreInteractions(caseDataRepository);
    }
}
