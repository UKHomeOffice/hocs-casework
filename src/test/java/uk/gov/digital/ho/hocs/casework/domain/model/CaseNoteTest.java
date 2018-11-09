package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseNoteTest {

    @Test
    public void getCaseNote() {

        UUID caseUUID = UUID.randomUUID();
        CaseNoteType type = CaseNoteType.MANUAL;
        String text = "anyText";

        CaseNote caseNote = new CaseNote(caseUUID, type, text);

        assertThat(caseNote.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseNote.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseNote.getCaseNoteType()).isEqualTo(type);
        assertThat(caseNote.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(caseNote.getText()).isEqualTo(text);

    }

    @Test(expected = EntityCreationException.class)
    public void getCaseNoteNullCaseUUID() {

        CaseNoteType type = CaseNoteType.MANUAL;
        String text = "anyText";

        new CaseNote(null, type, text);
    }

    @Test(expected = EntityCreationException.class)
    public void getCaseNoteNullType() {

        UUID caseUUID = UUID.randomUUID();
        String text = "anyText";

        new CaseNote(caseUUID, null, text);
    }

    @Test(expected = EntityCreationException.class)
    public void getCaseNoteNullText() {

        UUID caseUUID = UUID.randomUUID();
        CaseNoteType type = CaseNoteType.MANUAL;

        new CaseNote(caseUUID, type, null);
    }

}