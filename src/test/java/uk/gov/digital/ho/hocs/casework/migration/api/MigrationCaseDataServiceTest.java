package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CaseAttachment;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    private MigrationAuditClient migrationAuditClient;

    @Mock
    private DocumentClient documentClient;

    @Before
    public void setUp() {
        this.migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, documentClient, infoClient, migrationAuditClient);
    }

    @Test
    public void shouldCreateMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType.getDisplayCode(), data,
            originalReceivedDate);

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

    @Test()
    public void shouldAddCaseAttachments() {
        UUID caseId = UUID.randomUUID();
        CaseAttachment caseAttachment1 = new CaseAttachment("","","");
        CaseAttachment caseAttachment2 = new CaseAttachment("","","");
        List<CaseAttachment> caseAttachments = new ArrayList<>(List.of(caseAttachment1,caseAttachment2));
        CreateCaseworkDocumentRequest document1 = new CreateCaseworkDocumentRequest(caseAttachment1.getDisplayName(), caseAttachment1.getType(), caseAttachment1.getS3UntrustedUrl(), caseId);
        CreateCaseworkDocumentRequest document2 = new CreateCaseworkDocumentRequest(caseAttachment2.getDisplayName(), caseAttachment2.getType(), caseAttachment2.getS3UntrustedUrl(), caseId);
        migrationCaseDataService.createCaseAttachments(caseId, caseAttachments);

        verify(documentClient, times(2)).createDocument(any(UUID.class), any(CreateCaseworkDocumentRequest.class));
    }
}
