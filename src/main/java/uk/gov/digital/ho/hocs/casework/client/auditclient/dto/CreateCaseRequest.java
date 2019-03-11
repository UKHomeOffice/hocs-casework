package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    public static CreateCaseRequest from(CaseData caseData) {
     return new CreateCaseRequest(caseData.getUuid(),
             caseData.getCreated(),
             caseData.getType(),
             caseData.getReference(),
             caseData.getCaseDeadline(),
             caseData.getDateReceived());
    }
}