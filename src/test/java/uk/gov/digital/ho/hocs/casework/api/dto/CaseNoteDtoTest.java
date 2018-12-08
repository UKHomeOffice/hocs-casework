package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseNoteDtoTest {

    @Test
    public void getCaseNoteDto() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String text = "anyText";

        CaseNote caseNote = new CaseNote(caseUUID, type, text);

        CaseNoteDto caseNoteDto = CaseNoteDto.from(caseNote);

        assertThat(caseNoteDto.getUuid()).isEqualTo(caseNote.getUuid());
        assertThat(caseNoteDto.getCreated()).isEqualTo(caseNote.getCreated());
        assertThat(caseNoteDto.getType()).isEqualTo(caseNote.getCaseNoteType());
        assertThat(caseNoteDto.getCaseUUID()).isEqualTo(caseNote.getCaseUUID());
        assertThat(caseNoteDto.getText()).isEqualTo(caseNote.getText());

    }

}