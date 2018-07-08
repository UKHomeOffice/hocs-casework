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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private AuditService auditService;

    @Mock
    private CaseDataRepository caseDataRepository;

    private CaseDataService caseDataService;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(
                caseDataRepository,
                auditService
        );
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase(CaseType.MIN);

        verify(auditService, times(1)).writeCreateCaseEvent(eq(caseData));
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));

        assertThat(caseData).isNotNull();
        assertThat(caseData.getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateCaseCreateException1() throws EntityCreationException {
        caseDataService.createCase(null);
    }

    @Test()
    public void shouldCreateCaseCreateException2() {
        try {
            caseDataService.createCase(null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateCaseEvent(any(CaseData.class));
        verify(caseDataRepository, times(0)).save(any(CaseData.class));

    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData(CaseType.MIN.toString(), 123L));

        caseDataService.updateCase(caseUUID);

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));
        verify(auditService, times(1)).writeUpdateCaseEvent(any(CaseData.class));

    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateCaseMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        caseDataService.updateCase(null);
    }

    @Test()
    public void shouldUpdateCaseMissingUUIDException2() throws EntityNotFoundException {
        try {
            caseDataService.updateCase(null);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldUpdateCaseNotFound1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.updateCase(caseUUID);
    }

    @Test
    public void shouldUpdateCaseNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.updateCase(caseUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any());
    }

    @Test
    public void shouldGetCase() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData(CaseType.MIN.toString(), 1L));

        CaseData caseData = caseDataService.getCase(caseUUID);

        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(auditService, times(1)).writeGetCaseEvent(any());

        assertThat(caseData).isNotNull();
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseMissingUUID1() throws EntityNotFoundException {
        caseDataService.getCase(null);
    }

    @Test
    public void shouldGetCaseMissingUUID2() {

        try {
            caseDataService.getCase(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(auditService, times(0)).writeGetCaseEvent(any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNotFoundException1() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.getCase(caseUUID);
    }

    @Test
    public void shouldGetCaseNotFoundException2() {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.getCase(caseUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(auditService, times(1)).writeGetCaseEvent(any());
    }
}
