package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCorrespondentOutlineResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("fullname")
    private String fullname;

    public static GetCorrespondentOutlineResponse from(Correspondent correspondent) {
        return new GetCorrespondentOutlineResponse(correspondent.getUuid(), correspondent.getFullName());
    }
    public static GetCorrespondentOutlineResponse from(CorrespondentRepository.UuidToNamePair correspondent) {
        return new GetCorrespondentOutlineResponse(correspondent.getUuid(), correspondent.getFullname());
    }

}
