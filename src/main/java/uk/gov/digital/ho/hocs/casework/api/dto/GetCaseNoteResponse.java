package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor()
@Getter
public class GetCaseNoteResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z", timezone = "UTC")
    private ZonedDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("text")
    private String text;

    @JsonProperty("author")
    private String author;

    public static GetCaseNoteResponse from(CaseNote caseNote) {
        return new GetCaseNoteResponse(
                caseNote.getUuid(),
                ZonedDateTime.of(caseNote.getCreated(), ZoneOffset.UTC),
                caseNote.getCaseNoteType(),
                caseNote.getCaseUUID(),
                caseNote.getText(),
                caseNote.getAuthor());
    }
}
