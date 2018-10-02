package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;


import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetPrimaryTopicResponse {

    @JsonProperty("topic")
    private UUID uuid;

    public static GetPrimaryTopicResponse from(InputData inputData) {
        UUID topicUUID = null;
        try {
            HashMap<String,String> result = new ObjectMapper().readValue(inputData.getData(), HashMap.class);
            topicUUID = UUID.fromString(result.get("Topics"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new GetPrimaryTopicResponse(topicUUID);
    }
}
