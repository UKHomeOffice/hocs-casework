package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("stages")
    private Set<GetStageResponse> stages;

    @JsonProperty("documents")
    private Set<GetDocumentResponse> documents;

    public static GetCaseResponse from(CaseData caseData) {
        return new GetCaseResponse(
                caseData.getUuid(),
                caseData.getType(),
                caseData.getReference(),
                caseData.getTimestamp(),
                caseData.getStages()
                        .stream()
                        .map(GetStageResponse::from)
                        .collect(Collectors.toSet()),
                caseData.getDocuments()
                        .stream()
                        .map(GetDocumentResponse::from)
                        .collect(Collectors.toSet())
        );
    }
}
