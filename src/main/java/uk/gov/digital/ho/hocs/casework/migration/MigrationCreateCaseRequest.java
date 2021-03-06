package uk.gov.digital.ho.hocs.casework.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequestInterface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MigrationCreateCaseRequest implements CreateCaseRequestInterface {

    @JsonProperty("type")
    private String type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("deadline")
    private LocalDate caseDeadline;

    @JsonProperty("caseCreated")
    private LocalDateTime caseCreated;

    @JsonProperty("received")
    private LocalDate dateReceived;

    @JsonProperty("notes")
    private List<String> notes;

    @JsonProperty("totalsListName")
    private String totalsListName;

}