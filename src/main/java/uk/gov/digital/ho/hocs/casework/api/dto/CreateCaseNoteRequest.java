package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseNoteRequest {

    @NotEmpty
    @JsonProperty(value="type")
    private String type;

    @NotEmpty
    @JsonProperty(value="text")
    private String text;

}