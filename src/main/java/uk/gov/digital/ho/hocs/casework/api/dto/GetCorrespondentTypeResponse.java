package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetCorrespondentTypeResponse {

    @JsonProperty("correspondentTypes")
    Set<CorrespondentTypeDto> correspondentTypes;

    public static GetCorrespondentTypeResponse from(Set<CorrespondentTypeDto> correspondentTypeSet) {
        return new GetCorrespondentTypeResponse(correspondentTypeSet);
    }
}
