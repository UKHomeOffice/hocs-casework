package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateTopicRequest {

    @NonNull
    @JsonProperty(value = "topicUUID", required = true)
    private UUID topicUUID;

}
