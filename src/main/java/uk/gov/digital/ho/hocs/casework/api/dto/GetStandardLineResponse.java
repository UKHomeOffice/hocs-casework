package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class GetStandardLineResponse {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("topicUUID")
    private UUID topicUUID;

    @JsonProperty("expires")
    private LocalDateTime expires;

}
