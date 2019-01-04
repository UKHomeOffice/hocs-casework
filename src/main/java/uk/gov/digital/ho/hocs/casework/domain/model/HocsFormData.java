package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class HocsFormData {

    @Getter
    @JsonProperty("data")
    private HocsFormField data;

}
