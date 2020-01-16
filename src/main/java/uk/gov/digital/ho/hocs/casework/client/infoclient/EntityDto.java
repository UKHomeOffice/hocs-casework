package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntityDto<T> {

    @JsonProperty("simpleName")
    private String simpleName;

    @JsonProperty("data")
    private T data;
}
