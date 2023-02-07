package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateMigrationCaseAttachmentRequest {

    @JsonProperty("caseId")
    private UUID caseId;

    @JsonProperty("caseAttachments")
    private List<CaseAttachment> attachments;

}