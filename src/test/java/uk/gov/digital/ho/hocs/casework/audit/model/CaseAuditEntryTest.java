package uk.gov.digital.ho.hocs.casework.audit.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CaseAuditEntryTest {

    @Test
    public void shouldConstructAllValues() {

        CaseData caseData = new CaseData();

        CaseAuditEntry caseAuditEntry = CaseAuditEntry.from(caseData);

        assertThat(caseAuditEntry.getUuid()).isEqualTo(caseData.getUuid());
        //assertThat(caseAuditEntry.getReference()).isEqualTo(caseData.getReference());
        //assertThat(caseAuditEntry.getType()).isEqualTo(caseData.getType());
        assertThat(caseAuditEntry.getTimestamp()).isEqualTo(caseData.getTimestamp());
    }

    @Test
    public void shouldConstructAllValuesNull() {

        CaseAuditEntry caseAuditEntry = CaseAuditEntry.from(null);

        assertThat(caseAuditEntry).isNull();
    }

}