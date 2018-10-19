package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNote;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseNoteDto {

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("caseNote")
    private String caseNote;

    public static CaseNoteDto from(CaseNote caseNote) {
        return new CaseNoteDto(caseNote.getCreated(), caseNote.getText());
    }
}
