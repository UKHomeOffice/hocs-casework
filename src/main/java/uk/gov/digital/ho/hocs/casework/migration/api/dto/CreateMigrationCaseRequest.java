package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateMigrationCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("dateCreated")
    private LocalDate dateCreated;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("dateCompleted")
    private LocalDate dateCompleted;

    @JsonProperty("fromCaseUUID")
    private UUID fromCaseUUID;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("migratedReference")
    private String migratedReference;

}
