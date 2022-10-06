package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@EqualsAndHashCode
public class ComplaintCorrespondent {

    @NonNull
    @JsonProperty(value = "type", required = true)
    CorrespondentType type;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullname", required = true)
    String fullname;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    @JsonCreator
    public ComplaintCorrespondent(@NonNull @NotEmpty @JsonProperty("fullname") String fullname, @NonNull @NotEmpty @JsonProperty("type") CorrespondentType type) {
        this.fullname = fullname;
        this.type = type;
    }

}
