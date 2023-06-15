package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreatePrimaryTopicRequest {
    @JsonProperty("caseId")
    private UUID caseId;

    @JsonProperty("stageId")
    private UUID stageId;

    @JsonProperty("topicId")
    private UUID topicId;
}
