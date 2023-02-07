package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateMigrationCorrespondentRequest {

    @JsonProperty("caseId")
    private UUID caseId;

    @JsonProperty("stageId")
    private UUID stageId;

    @JsonProperty("primaryCorrespondent")
    private MigrationComplaintCorrespondent primaryCorrespondent;

    @JsonProperty("additionalCorrespondents")
    private List<MigrationComplaintCorrespondent> additionalCorrespondents;

}
