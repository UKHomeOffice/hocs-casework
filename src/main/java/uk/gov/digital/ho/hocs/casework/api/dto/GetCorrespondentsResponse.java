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
public class GetCorrespondentsResponse {

    @JsonProperty("correspondents")
    Set<CorrespondentDto> correspondents;

    public static GetCorrespondentsResponse from(Set<Correspondent> correspondentData) {
        Set<CorrespondentDto> correspondents = correspondentData
                .stream()
                .map(CorrespondentDto::from)
                .collect(Collectors.toSet());

        return new GetCorrespondentsResponse(correspondents);
    }
}
