package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseNoteResponseTest {

    @Test
    public void getCreateCaseNoteResponse() {
        UUID uuid = UUID.randomUUID();
        CaseNote caseNote = new CaseNote(1L,uuid, LocalDateTime.now(), "TYPE", UUID.randomUUID(),"text" );
        CreateCaseNoteResponse createCaseNoteResponse = CreateCaseNoteResponse.from(caseNote);
        assertThat(createCaseNoteResponse.getUuid()).isEqualTo(caseNote.getUuid());
    }



}