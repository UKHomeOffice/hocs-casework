package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository mockAuditRepository;

    @Mock
    private RequestData mockRequestData;

    @Mock
    private ObjectMapper objectMapper;

    private AuditService auditService;

    @Captor
    private ArgumentCaptor<AuditEntry> argCaptor;

    @Before
    public void setUp() {
        this.auditService = new AuditService(mockAuditRepository, mockRequestData, objectMapper);
    }

    @Test
    public void shouldWriteGetCaseEvent() {
        auditService.getCaseEvent(UUID.randomUUID());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.GET_CASE.toString());
    }

    @Test
    public void shouldWriteGetCaseEventNull() {
        auditService.getCaseEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.GET_CASE.toString());
    }

    @Test
    public void shouldWriteCreateCaseEvent() {
        CaseType caseType = CaseType.MIN;
        auditService.createCaseEvent(new CaseData(caseType, 0l));
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldWriteCreateCaseEventNull() {
        auditService.createCaseEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldWriteUpdateCaseEvent() {
        CaseType caseType = CaseType.MIN;
        auditService.updateCaseEvent(new CaseData(caseType, 0l));
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_CASE.toString());
    }

    @Test
    public void shouldWriteUpdateCaseEventNull() {
        auditService.updateCaseEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_CASE.toString());
    }

    @Test
    public void shouldWriteCreateStageEvent() {
        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        auditService.createStageEvent(uuid, stageType, uuid, uuid);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldWriteCreateStageEventNull() {
        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        auditService.createStageEvent(uuid, stageType, uuid, uuid);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldWriteUpdateStageEvent() {
        auditService.updateInputDataEvent(new InputData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());
    }

    @Test
    public void shouldWriteUpdateStageEventNull() {
        auditService.updateInputDataEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());
    }

    @Test
    public void shouldWriteAddDocumentEvent() {
        DocumentData documentData = new DocumentData();
        auditService.createDocumentEvent(documentData);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteAddDocumentEventNull() {
        auditService.createDocumentEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteUpdateDocumentEvent() {
        DocumentData documentData = new DocumentData();
        auditService.updateDocumentEvent(documentData);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteUpdateDocumentEventNull() {
        auditService.updateDocumentEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_DOCUMENT.toString());
    }

    @Test
    public void shouldExtractEvent() {
        auditService.extractReportEvent("");
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CSV_EXTRACT.toString());
    }

    @Test
    public void shouldExtractEventNull() {
        auditService.extractReportEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CSV_EXTRACT.toString());
    }

}
