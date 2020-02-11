package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class EntityTotalDto {

    @JsonProperty("addFields")
    private Map<String, String> addFields;

    @JsonProperty("subFields")
    private Map<String, String> subFields;
}
