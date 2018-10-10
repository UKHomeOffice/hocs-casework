package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseNoteResponse {

    @JsonProperty("label")
    private LocalDateTime created;

    @JsonProperty("value")
    private String caseNote;

    public static GetCaseNoteResponse from(CaseNoteData caseNoteData) {
        return new GetCaseNoteResponse(caseNoteData.getCreated(), caseNoteData.getCaseNote());
    }
}