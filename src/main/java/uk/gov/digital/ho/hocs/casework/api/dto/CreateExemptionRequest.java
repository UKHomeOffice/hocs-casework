package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;


@AllArgsConstructor
@Getter
public class CreateExemptionRequest {

    @NonNull
    @JsonProperty(value = "type", required = true)
    String type;

}
