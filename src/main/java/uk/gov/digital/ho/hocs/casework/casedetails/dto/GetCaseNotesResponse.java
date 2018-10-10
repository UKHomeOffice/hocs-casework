package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseNotesResponse {

    @JsonProperty("casenotes")
    private Set<GetCaseNoteResponse> caseNotes;

    public static GetCaseNotesResponse from(Set<CaseNoteData> caseNoteData) {
        Set<GetCaseNoteResponse> caseNotesResponses = caseNoteData
                .stream()
                .map(GetCaseNoteResponse::from)
                .collect(Collectors.toSet());

        return new GetCaseNotesResponse(caseNotesResponses);
    }
}