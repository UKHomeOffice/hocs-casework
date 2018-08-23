package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetCorrespondentResponse {

    @JsonProperty("correspondents")
    Set<CorrespondentDto> correspondents;

    public static GetCorrespondentResponse from(Set<CorrespondentData> correspondentData) {
        Set<CorrespondentDto> correspondents = correspondentData
                .stream()
                .map(CorrespondentDto::from)
                .collect(Collectors.toSet());

        return new GetCorrespondentResponse(correspondents);
    }
}
