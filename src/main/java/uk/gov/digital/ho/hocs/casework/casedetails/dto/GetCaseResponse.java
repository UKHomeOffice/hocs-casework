package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @JsonProperty("deadlines")
    private Set<DeadlineDataDto> deadlines;

    @JsonProperty("documents")
    private Set<GetDocumentResponse> documents;

    @JsonProperty("data")
    private String data;

    public static GetCaseResponse from(CaseData caseData) {
        Set<GetStageResponse> stageResponses = caseData.getStages()
                .stream()
                .map(GetStageResponse::from)
                .collect(Collectors.toSet());

        Set<GetDocumentResponse> documentResponses = caseData.getDocuments()
                .stream()
                .map(GetDocumentResponse::from)
                .collect(Collectors.toSet());

        Set<DeadlineDataDto> deadlineDataDtos = caseData.getDeadline()
                .stream()
                .map(d -> new DeadlineDataDto(d.getStage(), d.getDate()))
                .collect(Collectors.toSet());

        String caseRef = null;
        String data = null;
        InputData inputData = caseData.getInputData();
        if (inputData != null) {
            caseRef = caseData.getReference();
            data = inputData.getData();
        }

        return new GetCaseResponse(
                caseData.getTypeString(),
                caseRef,
                caseData.getUuid(),
                caseData.getCreated(),
                stageResponses,
                deadlineDataDtos,
                documentResponses,
                data
        );
    }
}
