package uk.gov.digital.ho.hocs.casework.audit;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditEntryTest {

    @Test
    public void createAuditEntry() {
        String uuid = "1234";
        String action = "TEST";
        String timestamp = "01/01/2018";
        String username = "Test User";
        String caseData = "{test: value}";

        AuditEntry entry = new AuditEntry(uuid, timestamp, action, username, caseData);

        assertThat(entry.getUuid()).isEqualTo(uuid);
        assertThat(entry.getAction()).isEqualTo(action);
        assertThat(entry.getTimestamp()).isEqualTo(timestamp);
        assertThat(entry.getUsername()).isEqualTo(username);
        assertThat(entry.getCaseData()).isEqualTo(caseData);
    }

}
