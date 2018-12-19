package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCaseNoteResponseTest {

    @Test
    public void getCaseNoteDto() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String text = "anyText";

        CaseNote caseNote = new CaseNote(caseUUID, type, text);

        GetCaseNoteResponse getCaseNotResponse = GetCaseNoteResponse.from(caseNote);

        assertThat(getCaseNotResponse.getUuid()).isEqualTo(caseNote.getUuid());
        assertThat(getCaseNotResponse.getCreated()).isEqualTo(caseNote.getCreated());
        assertThat(getCaseNotResponse.getType()).isEqualTo(caseNote.getCaseNoteType());
        assertThat(getCaseNotResponse.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
        assertThat(getCaseNotResponse.getText()).isEqualTo(caseNote.getText());

    }

}