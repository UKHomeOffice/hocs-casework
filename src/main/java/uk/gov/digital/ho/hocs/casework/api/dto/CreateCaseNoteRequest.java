package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
public class CreateCaseNoteRequest {

    @NotEmpty
    @JsonProperty(value="type")
    private String type;

    @NotEmpty
    @JsonProperty(value="text")
    private String text;

    @NotEmpty
    @JsonProperty(value="stageType")
    private String stageType;
}