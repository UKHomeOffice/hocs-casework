package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class DeleteCaseAuditResponse {

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("deleted")
    private Boolean deleted;

    @JsonProperty("auditCount")
    private Integer auditCount;
}
