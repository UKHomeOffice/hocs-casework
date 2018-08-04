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

    @JsonProperty("type")
    private String caseType;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("stages")
    private Set<GetStageResponse> stages;

    @JsonProperty("documents")
    private Set<GetDocumentResponse> documents;

    public static GetCaseResponse from(CaseData caseData) {
        Set<GetStageResponse> stageResponses = caseData.getStages()
                .stream()
                .map(GetStageResponse::from)
                .collect(Collectors.toSet());

        Set<GetDocumentResponse> documentResponses = caseData.getDocuments()
                .stream()
                .map(GetDocumentResponse::from)
                .collect(Collectors.toSet());

        return new GetCaseResponse(
                caseData.getType().toString(),
                caseData.getReference(),
                caseData.getUuid(),
                caseData.getTimestamp(),
                stageResponses,
                documentResponses
        );
    }
}
