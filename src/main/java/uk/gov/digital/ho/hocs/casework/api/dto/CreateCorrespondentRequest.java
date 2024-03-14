package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import jakarta.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
public class CreateCorrespondentRequest {

    @NonNull
    @JsonProperty(value = "type", required = true)
    String type;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullname", required = true)
    String fullname;

    @JsonProperty(value = "organisation")
    String organisation;

    @JsonProperty("postcode")
    String postcode;

    @JsonProperty("address1")
    String address1;

    @JsonProperty("address2")
    String address2;

    @JsonProperty("address3")
    String address3;

    @JsonProperty("country")
    String country;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    @JsonProperty("reference")
    String reference;

    @JsonProperty("externalKey")
    String externalKey;

}
