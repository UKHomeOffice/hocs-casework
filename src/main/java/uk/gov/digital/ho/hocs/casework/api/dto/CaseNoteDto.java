package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseNoteDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("text")
    private String text;

    public static CaseNoteDto from(CaseNote caseNote) {
        return new CaseNoteDto(
                caseNote.getUuid(),
                caseNote.getCreated(),
                caseNote.getCaseNoteType().toString(),
                caseNote.getCaseUUID(),
                caseNote.getText());
    }
}
