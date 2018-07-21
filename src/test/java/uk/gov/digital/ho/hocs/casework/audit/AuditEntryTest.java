package uk.gov.digital.ho.hocs.casework.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.model.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuditEntryTest {

    private static String userName = "anyUserName";

    @Test
    public void shouldConstructCaseData() {
        CaseData caseData = new CaseData();

        AuditEntry auditEntry = new AuditEntry(userName, caseData, AuditAction.CREATE_CASE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getCaseInstance()).isEqualTo(CaseAuditEntry.from(caseData));
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldConstructCaseDataNull() {
        AuditEntry auditEntry = new AuditEntry(userName, (CaseData) null, AuditAction.CREATE_CASE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getCaseInstance()).isEqualTo(null);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
    }

    @Test
    public void shouldConstructStageData() {
        StageData stageData = new StageData();

        AuditEntry auditEntry = new AuditEntry(userName, stageData, AuditAction.CREATE_STAGE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getStageInstance()).isEqualTo(StageAuditEntry.from(stageData));
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldConstructStageDataNull() {
        AuditEntry auditEntry = new AuditEntry(userName, (StageData) null, AuditAction.CREATE_STAGE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getStageInstance()).isEqualTo(null);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldConstructDocumentData() {
        DocumentData documentData = new DocumentData();

        AuditEntry auditEntry = new AuditEntry(userName, documentData, AuditAction.ADD_DOCUMENT);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getDocumentInstance()).isEqualTo(DocumentAuditEntry.from(documentData));
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldConstructDocumentDataNull() {
        AuditEntry auditEntry = new AuditEntry(userName, (DocumentData) null, AuditAction.ADD_DOCUMENT);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getDocumentInstance()).isEqualTo(null);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.ADD_DOCUMENT.toString());
    }

    @Test
    public void shouldConstructQueryData() {
        String query = "";

        AuditEntry auditEntry = new AuditEntry(userName, query, AuditAction.SEARCH);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getQueryData()).isEqualTo(query);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());
    }

    @Test
    public void shouldConstructQueryDataNull() {
        AuditEntry auditEntry = new AuditEntry(userName, (String) null, AuditAction.SEARCH);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getQueryData()).isEqualTo(null);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());
    }

}
