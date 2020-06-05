package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCorrespondentsResponse {

    @JsonProperty("correspondents")
    Set<GetCorrespondentWithPrimaryFlagResponse> correspondents;

    public static GetCorrespondentsResponse from(Set<CorrespondentWithPrimaryFlag> correspondentData) {
        Set<GetCorrespondentWithPrimaryFlagResponse> correspondents = correspondentData
                .stream()
                .map(GetCorrespondentWithPrimaryFlagResponse::from)
                .collect(Collectors.toSet());

        return new GetCorrespondentsResponse(correspondents);
    }

}
