package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseNoteTest {

    @Test
    public void getCaseNote() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String text = "anyText";
        String stageType = "anyType";

        CaseNote caseNote = new CaseNote(caseUUID, type, text, stageType);

        assertThat(caseNote.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(caseNote.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(caseNote.getCaseNoteType()).isEqualTo(type);
        assertThat(caseNote.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(caseNote.getText()).isEqualTo(text);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseNoteNullCaseUUID() {

        String type = "MANUAL";
        String text = "anyText";
        String stageType = "anyType";

        new CaseNote(null, type, text, stageType);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseNoteNullType() {

        UUID caseUUID = UUID.randomUUID();
        String text = "anyText";
        String stageType = "anyType";

        new CaseNote(caseUUID, null, text, stageType);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseNoteNullText() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String stageType = "anyType";

        new CaseNote(caseUUID, type, null, stageType);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCaseNoteNullStageType() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String text = "anyText";

        new CaseNote(caseUUID, type, text, null);

    }

}