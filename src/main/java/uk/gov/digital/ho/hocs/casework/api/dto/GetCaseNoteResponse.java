package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor()
@Getter
public class GetCaseNoteResponse {

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

    public static GetCaseNoteResponse from(CaseNote caseNote) {
        return new GetCaseNoteResponse(
                caseNote.getUuid(),
                caseNote.getCreated(),
                caseNote.getCaseNoteType(),
                caseNote.getCaseUUID(),
                caseNote.getText());
    }
}
