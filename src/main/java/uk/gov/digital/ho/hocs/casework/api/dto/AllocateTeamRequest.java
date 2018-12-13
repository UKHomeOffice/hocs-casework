package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AllocateTeamRequest {

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("allocationType")
    private String allocationType;
}
