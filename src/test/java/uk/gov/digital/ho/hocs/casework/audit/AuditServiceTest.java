package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.rsh.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    public void shouldWriteSearchEvent() {
        auditService.writeSearchEvent(new SearchRequest());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());
    }

    @Test
    public void shouldWriteSearchEventNull() {
        auditService.writeSearchEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());
    }

    @Test
    public void shouldWriteSendEmailEvent() {
        auditService.writeSendEmailEvent(new SendEmailRequest());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEND_EMAIL.toString());
    }

    @Test
    public void shouldWriteSendEmailEventNull() {
        auditService.writeSendEmailEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEND_EMAIL.toString());
    }

    @Test
    public void shouldWriteGetCaseEvent() {
        auditService.writeGetCaseEvent(UUID.randomUUID());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.GET_CASE.toString());
    }

    @Test
    public void shouldWriteGetCaseEventNull() {
        auditService.writeGetCaseEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.GET_CASE.toString());
    }

    @Test
    public void shouldWriteCreateCaseEvent() {
        auditService.writeCreateCaseEvent(new CaseData(), new CaseInputData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldWriteCreateCaseEventNull() {
        auditService.writeCreateCaseEvent(null, new CaseInputData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldWriteUpdateCaseEvent() {
        auditService.writeUpdateCaseEvent(new CaseData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_CASE.toString());
    }

    @Test
    public void shouldWriteUpdateCaseEventNull() {
        auditService.writeUpdateCaseEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_CASE.toString());
    }

    @Test
    public void shouldWriteCreateStageEvent() {
        auditService.writeCreateStageEvent(new StageData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldWriteCreateStageEventNull() {
        auditService.writeCreateStageEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldWriteUpdateStageEvent() {
        auditService.writeUpdateStageEvent(new StageData());
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());
    }

    @Test
    public void shouldWriteUpdateStageEventNull() {
        auditService.writeUpdateStageEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());
    }

    @Test
    public void shouldWriteAddDocumentEvent() {
        DocumentData documentData = new DocumentData();
        auditService.writeCreateDocumentEvent(documentData);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteAddDocumentEventNull() {
        auditService.writeCreateDocumentEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteUpdateDocumentEvent() {
        DocumentData documentData = new DocumentData();
        auditService.writeUpdateDocumentEvent(documentData);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_DOCUMENT.toString());
    }

    @Test
    public void shouldWriteUpdateDocumentEventNull() {
        auditService.writeUpdateDocumentEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_DOCUMENT.toString());
    }

    @Test
    public void shouldExtractEvent() {
        auditService.writeExtractEvent("");
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CSV_EXTRACT.toString());
    }

    @Test
    public void shouldExtractEventNull() {
        auditService.writeExtractEvent(null);
        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));

        verify(mockAuditRepository).save(argCaptor.capture());
        AuditEntry auditEntry = argCaptor.getValue();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CSV_EXTRACT.toString());
    }

}
