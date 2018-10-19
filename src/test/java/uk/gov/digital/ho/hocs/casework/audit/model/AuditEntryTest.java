package uk.gov.digital.ho.hocs.casework.audit.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuditEntryTest {

    private static String userName = "anyUserName";

    @Test
    public void shouldConstructCaseData() {
        CaseData caseData = new CaseData(CaseDataType.MIN, 0l);

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
        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        AuditEntry auditEntry = new AuditEntry(userName, stage, AuditAction.CREATE_STAGE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getStageInstance()).isEqualTo(StageAuditEntry.from(stage));
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
    }

    @Test
    public void shouldConstructStageDataNull() {
        AuditEntry auditEntry = new AuditEntry(userName, (Stage) null, AuditAction.CREATE_STAGE);

        assertThat(auditEntry.getUsername()).isEqualTo(userName);
        assertThat(auditEntry.getStageInstance()).isEqualTo(null);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
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
