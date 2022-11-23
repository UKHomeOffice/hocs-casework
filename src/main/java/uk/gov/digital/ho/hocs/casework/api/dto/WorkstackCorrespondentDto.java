package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.Correspondent;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WorkstackCorrespondentDto {

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("type")
    private String type;

    @JsonProperty("is_primary")
    private boolean isPrivate;

    public static WorkstackCorrespondentDto from(Correspondent correspondent, UUID primaryCorrespondentUUID) {
        return new WorkstackCorrespondentDto(correspondent.getFullName(), correspondent.getPostcode(),
            correspondent.getCorrespondentType(), correspondent.getUuid().equals(primaryCorrespondentUUID));

    }

}
