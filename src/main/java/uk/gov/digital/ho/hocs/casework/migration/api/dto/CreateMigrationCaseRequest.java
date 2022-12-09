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
public class CreateMigrationCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("fromCaseUUID")
    private UUID fromCaseUUID;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("primaryCorrespondent")
    private MigrationComplaintCorrespondent primaryCorrespondent;

    @JsonProperty("additionalCorrespondents")
    private List<MigrationComplaintCorrespondent> additionalCorrespondents;

    @JsonProperty("caseAttachments")
    private List<CaseAttachment> attachments;
}
