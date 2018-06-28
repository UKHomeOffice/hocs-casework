package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentToCaseRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private AuditService auditService;
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private StageDataRepository stageDataRepository;
    @Mock
    private DocumentService documentService;
    @Mock
    private RequestData requestData;

    private CaseDataService caseDataService;

    private final String testUser = "Test User";
    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(
                caseDataRepository,
                stageDataRepository,
                auditService,
                documentService,
                requestData
        );
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase("Type");

        verify(auditService, times(1)).writeCreateCaseEvent(eq(caseData));
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));

        assertThat(caseData).isNotNull();
        assertThat(caseData.getType()).isEqualTo("Type");
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
    public void shouldCreateStage() throws EntityCreationException {
        StageData stageData = caseDataService.createStage(uuid, "CREATE", new HashMap<>());

        verify(auditService).writeCreateStageEvent(eq(stageData));
        verify(stageDataRepository).save(isA(StageData.class));

        assertThat(stageData).isNotNull();
        assertThat(stageData.getType()).isEqualTo("CREATE");
        assertThat(stageData.getData()).isEqualTo("{ }");
        assertThat(stageData.getCaseUUID()).isEqualTo(uuid);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingUUIDException1() throws EntityCreationException {
        caseDataService.createStage(null, "CREATE", new HashMap<>());
    }

    @Test()
    public void shouldCreateStageMissingUUIDException2() {
        try {
            caseDataService.createStage(null, "CREATE", new HashMap<>());
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingTypeException1() throws EntityCreationException {
        caseDataService.createStage(uuid, null, new HashMap<>());
    }

    @Test()
    public void shouldCreateStageMissingTypeException2() {
        try {
            caseDataService.createStage(uuid, null, new HashMap<>());
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData("Type", 123L));

        CaseData caseData = caseDataService.updateCase(caseUUID, "Type");

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));
        verify(auditService, times(1)).writeUpdateCaseEvent(eq(caseData));

        assertThat(caseData).isNotNull();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateCaseMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        caseDataService.updateCase(null, "Type");
    }

    @Test()
    public void shouldUpdateCaseMissingUUIDException2() throws EntityNotFoundException {
        try {
            caseDataService.updateCase(null, "Type");
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateCaseMissingTypeException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>());
    }

    @Test()
    public void shouldUpdateCaseMissingTypeException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateCase(caseUUID, null);
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

        caseDataService.updateCase(caseUUID, "Type");
    }

    @Test
    public void shouldUpdateCaseNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.updateCase(caseUUID, "Type");
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any());
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(new StageData(uuid, "CREATE", "Some data"));

        StageData stageData = caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>());

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(stageDataRepository, times(1)).save(isA(StageData.class));
        verify(auditService, times(1)).writeUpdateStageEvent(eq(stageData));

        assertThat(stageData).isNotNull();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateStageMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, null, "CREATE", new HashMap<>());
    }

    @Test()
    public void shouldUpdateStageMissingUUIDException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateStage(caseUUID, null, "CREATE", new HashMap<>());
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(stageDataRepository, times(0)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateStageMissingTypeException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>());
    }

    @Test()
    public void shouldUpdateStageMissingTypeException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>());
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(stageDataRepository, times(0)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldUpdateStageNotFound1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>());
    }

    @Test
    public void shouldUpdateStageNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>());
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(stageDataRepository, times(1)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any());
    }

    @Test
    public void shouldGetCase() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData("", 1L));

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

    @Test
    public void shouldAddDocumentToCase() throws EntityCreationException, EntityNotFoundException {
        AddDocumentToCaseRequest document = new AddDocumentToCaseRequest(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), "Test Document", DocumentType.DRAFT,
                "a link", "an original link", DocumentStatus.UPLOADED);


        caseDataService.addDocumentToCase(document);
        verify(documentService,times(1)).updateDocument(any(), any(), any(), any(), any());
    }

}
