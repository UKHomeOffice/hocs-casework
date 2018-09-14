package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class AddTopicToCaseRequest {

    @JsonProperty("topicUUID")
    private UUID topicUUID;

    @JsonProperty("topicName")
    private String topicName;

}