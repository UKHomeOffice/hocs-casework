package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
public class CaseSummaryLink {

    @JsonProperty("caseReference")
    @Getter
    private String caseReference;

    @JsonProperty("caseUUID")
    @Getter
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    @Getter
    private UUID stageUUID;

}
