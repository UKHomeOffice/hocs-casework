package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TemplateDto {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("documentUUID")
    private UUID documentUUID;

    @JsonProperty("caseType")
    private String caseType;

}
