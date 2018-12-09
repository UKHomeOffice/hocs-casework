package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class InfoTopic {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private UUID value;

}
