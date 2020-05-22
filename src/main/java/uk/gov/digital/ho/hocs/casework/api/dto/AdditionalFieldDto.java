package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AdditionalFieldDto {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private String value;

    @JsonProperty("type")
    private String type;

    @JsonProperty("choices")
    private Object choices;

    public static AdditionalFieldDto from(AdditionalField additionalField) {
        return new AdditionalFieldDto(additionalField.getLabel(), additionalField.getValue(), additionalField.getType(), additionalField.getChoices());
    }
}
