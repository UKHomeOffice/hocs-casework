package uk.gov.digital.ho.hocs.casework.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.casedetails.model.*;
import uk.gov.digital.ho.hocs.casework.rsh.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository mockAuditRepository;

    @Mock
    private RequestData mockRequestData;


    private AuditService auditService;

    private String testUser = "Test User";


    @Before
    public void setUp() {
        this.auditService = new AuditService(mockAuditRepository, mockRequestData);
    }

    @Test
    public void shouldWriteSearchEvent() {
        SearchRequest searchRequest = new SearchRequest();

        auditService.writeSearchEvent(searchRequest);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteSendEmailEvent() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest("", new HashMap<>());

        auditService.writeSendEmailEvent(sendEmailRequest);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteGetCaseEvent() {
        UUID caseUUID = UUID.randomUUID();

        auditService.writeGetCaseEvent(caseUUID);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteCreateCaseEvent() {
        CaseData caseData = new CaseData(CaseType.MIN.toString(), 1L);

        auditService.writeCreateCaseEvent(caseData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateCaseEvent() {
        CaseData caseData = new CaseData(CaseType.MIN.toString(), 1L);

        auditService.writeUpdateCaseEvent(caseData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteCreateStageEvent() {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");

        auditService.writeCreateStageEvent(stageData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateStageEvent() {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");

        auditService.writeUpdateStageEvent(stageData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteAddDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), "", DocumentType.ORIGINAL);

        auditService.writeAddDocumentEvent(documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldWriteUpdateDocumentEvent() {
        DocumentData documentData = new DocumentData(UUID.randomUUID(), "", DocumentType.ORIGINAL);

        auditService.writeUpdateDocumentEvent(documentData);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

    @Test
    public void shouldExtractEvent() {
        String params = "";

        auditService.writeExtractEvent(params);

        verify(mockAuditRepository, times(1)).save(any(AuditEntry.class));
    }

}
