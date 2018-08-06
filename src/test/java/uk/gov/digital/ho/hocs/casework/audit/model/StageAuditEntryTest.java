package uk.gov.digital.ho.hocs.casework.audit.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StageAuditEntryTest {

    @Test
    public void shouldConstructAllValues() {
        CaseData caseData = new CaseData(CaseType.RSH, 1L);

        StageData stageData = new StageData(caseData, StageType.DCU_MIN_MARKUP, new HashMap<>(), new ObjectMapper());

        StageAuditEntry stageAuditEntry = StageAuditEntry.from(stageData);

        assertThat(stageAuditEntry.getUuid()).isEqualTo(stageData.getUuid());
        assertThat(stageAuditEntry.getCaseUUID()).isEqualTo(stageData.getCaseUUID());
        assertThat(stageAuditEntry.getType()).isEqualTo(stageData.getType());
        assertThat(stageAuditEntry.getTimestamp()).isEqualTo(stageData.getTimestamp());
        assertThat(stageAuditEntry.getData()).isEqualTo(stageData.getData());

    }

    @Test
    public void shouldConstructAllValuesNull() {

        StageAuditEntry stageAuditEntry = StageAuditEntry.from(null);

        assertThat(stageAuditEntry).isNull();
    }

}