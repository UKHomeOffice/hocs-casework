package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCorrespondentOutlinesResponse {

    @JsonProperty("correspondents")
    Set<GetCorrespondentOutlineResponse> correspondents;

    public static GetCorrespondentOutlinesResponse from(Set<Correspondent> correspondentData) {
        Set<GetCorrespondentOutlineResponse> correspondents = correspondentData.stream().map(
            GetCorrespondentOutlineResponse::from).collect(Collectors.toSet());

        return new GetCorrespondentOutlinesResponse(correspondents);
    }

}
