package uk.gov.digital.ho.hocs.casework.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MigrationCreateCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("deadline")
    private LocalDate caseDeadline;

    @JsonProperty("received")
    private LocalDate dateReceived;

}