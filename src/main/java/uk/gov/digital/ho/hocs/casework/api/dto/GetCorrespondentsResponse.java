package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentType;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetCorrespondentsResponse {

    @JsonProperty("correspondents")
    Set<GetCorrespondentResponse> correspondents;

    public static GetCorrespondentsResponse from(Set<Correspondent> correspondentData) {
        Set<GetCorrespondentResponse> correspondents = correspondentData
                .stream()
                .map(cd -> GetCorrespondentResponse.from(cd, CorrespondentType.APPLICANT))
                .collect(Collectors.toSet());

        return new GetCorrespondentsResponse(correspondents);
    }
}
