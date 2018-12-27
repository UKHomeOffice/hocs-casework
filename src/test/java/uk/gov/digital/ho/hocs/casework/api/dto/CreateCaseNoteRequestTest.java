package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseNoteRequestTest {

    @Test
    public void getCreateCaseNoteRequest() {

        String type = "TYPE";
        String text = "Text";
        String stageType = "STAGETYPE";

        CreateCaseNoteRequest createCaseNoteRequest = new CreateCaseNoteRequest(type, text, stageType);
        assertThat(createCaseNoteRequest.getType()).isEqualTo(type);
        assertThat(createCaseNoteRequest.getText()).isEqualTo(text);
    }



}