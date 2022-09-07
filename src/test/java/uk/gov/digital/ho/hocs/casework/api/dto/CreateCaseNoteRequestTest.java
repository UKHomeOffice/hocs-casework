package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseNoteRequestTest {

    @Test
    public void getCreateCaseNoteRequest() {

        String type = "TYPE";
        String text = "Text";
        CreateCaseNoteRequest createCaseNoteRequest = new CreateCaseNoteRequest(type, text);
        assertThat(createCaseNoteRequest.getType()).isEqualTo(type);
        assertThat(createCaseNoteRequest.getText()).isEqualTo(text);
    }

}
