package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseNotesResponse {

    @JsonProperty("caseNotes")
    private Set<GetCaseNotResponse> caseNotes;

    public static GetCaseNotesResponse from(Set<CaseNote> caseNoteData) {
        Set<GetCaseNotResponse> caseNotesResponses = caseNoteData
                .stream()
                .map(GetCaseNotResponse::from)
                .collect(Collectors.toSet());

        return new GetCaseNotesResponse(caseNotesResponses);
    }
}
