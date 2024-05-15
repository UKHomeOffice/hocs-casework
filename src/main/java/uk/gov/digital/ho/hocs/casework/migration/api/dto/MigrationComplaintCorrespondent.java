package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.lang.NonNull;

import jakarta.validation.constraints.NotEmpty;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MigrationComplaintCorrespondent {

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullName", required = true)
    String fullName;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "correspondentType", required = true)
    CorrespondentType correspondentType;

    @JsonProperty("address1")
    String address1;

    @JsonProperty("address2")
    String address2;

    @JsonProperty("address3")
    String address3;

    @JsonProperty("postcode")
    String postcode;

    @JsonProperty("country")
    String country;

    @JsonProperty("organisation")
    String organisation;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    @JsonProperty("reference")
    String reference;

    public MigrationComplaintCorrespondent(@NonNull @NotEmpty  String fullName, @NonNull @NotEmpty CorrespondentType type) {
        this.correspondentType = type;
        this.fullName = fullName;
    }
}
