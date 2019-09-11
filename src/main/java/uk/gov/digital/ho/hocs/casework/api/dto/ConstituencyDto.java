package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class ConstituencyDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("constituencyName")
    private String constituencyName;

    @JsonProperty("regionUUID")
    private UUID regionUUID;

    @JsonProperty("active")
    private boolean active;
}
