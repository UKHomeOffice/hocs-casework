package uk.gov.digital.ho.hocs.casework.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.*;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository mockAuditRepository;

    private AuditService auditService;

    private String testUser = "Test User";


    @Before
    public void setUp() {
        this.auditService = new AuditService(mockAuditRepository);
    }

    @Test
    public void shouldWriteSearchEvent() {
        SearchRequest searchRequest = new SearchRequest();

        auditService.writeSearchEvent(testUser, searchRequest);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteSendEmailEvent() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest("", new HashMap<>());

        auditService.writeSendEmailEvent(testUser, sendEmailRequest);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteGetCaseEvent() {
        UUID caseUUID = UUID.randomUUID();

        auditService.writeGetCaseEvent(testUser, caseUUID);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteCreateCaseEvent() {
        CaseData caseData = new CaseData("", 1L);

        auditService.writeCreateCaseEvent(testUser, caseData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateCaseEvent() {
        CaseData caseData = new CaseData("", 1L);

        auditService.writeUpdateCaseEvent(testUser, caseData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteCreateStageEvent() {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");

        auditService.writeCreateStageEvent(testUser, stageData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateStageEvent() {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");

        auditService.writeUpdateStageEvent(testUser, stageData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteAddDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), UUID.randomUUID(), "",DocumentType.ORIGINAL);

        auditService.writeAddDocumentEvent(testUser, documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), UUID.randomUUID(), "",DocumentType.ORIGINAL);

        auditService.writeUpdateDocumentEvent(testUser, documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteDeleteDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), UUID.randomUUID(), "",DocumentType.ORIGINAL);

        auditService.writeDeleteDocumentEvent(testUser, documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUndeleteDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), UUID.randomUUID(), "",DocumentType.ORIGINAL);

        auditService.writeUndeleteDocumentEvent(testUser, documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldExtractEvent() {
        String params = "";

        auditService.writeExtractEvent(testUser, params);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

}
