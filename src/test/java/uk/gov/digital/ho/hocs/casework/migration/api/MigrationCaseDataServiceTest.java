package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseDataServiceTest {
    private static final long caseID = 12345L;

    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");

    private final String STAGE_TYPE = "Migration";

    private MigrationCaseDataService migrationCaseDataService;

    private final Map<String, String> data = new HashMap<>(0);

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private AuditClient auditClient;

    @Before
    public void setUp() {
        this.migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, infoClient, auditClient);
    }

    @Test
    public void shouldCreateMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType.getDisplayCode(), data, originalReceivedDate);

        // then
        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }


    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        migrationCaseDataService.createCompletedCase(null, new HashMap<>(), originalReceivedDate);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            migrationCaseDataService.createCompletedCase(null, new HashMap<>(), originalReceivedDate);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }
}
