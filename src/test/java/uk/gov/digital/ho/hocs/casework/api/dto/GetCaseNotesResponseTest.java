package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCaseNotesResponseTest {

    @Test
    public void getCaseNoteDto() {

        UUID caseUUID = UUID.randomUUID();
        String type = "MANUAL";
        String text = "anyText";

        CaseNote caseNote = new CaseNote(caseUUID, type, text);

        Set<CaseNote> caseNotes = new HashSet<>();
        caseNotes.add(caseNote);

        GetCaseNotesResponse getCaseNotesResponse = GetCaseNotesResponse.from(caseNotes);

        assertThat(getCaseNotesResponse.getCaseNotes()).hasSize(1);

    }

    @Test
    public void getCaseNoteDtoEmpty() {

        Set<CaseNote> caseNotes = new HashSet<>();

        GetCaseNotesResponse getCaseNotesResponse = GetCaseNotesResponse.from(caseNotes);

        assertThat(getCaseNotesResponse.getCaseNotes()).hasSize(0);

    }

}