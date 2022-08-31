package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseRequest implements CreateCaseRequestInterface {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("fromCaseUUID")
    private UUID fromCaseUUID;

}
