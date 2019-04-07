package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("received")
    private LocalDate dateReceieved;

}