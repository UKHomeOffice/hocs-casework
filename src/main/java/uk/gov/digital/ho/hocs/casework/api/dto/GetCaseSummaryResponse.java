package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseSummaryResponse {

    @JsonProperty("caseDeadline")
    LocalDate caseDeadline;

    @JsonProperty("stageDeadlines")
    Map<String, String> stageDeadlines;

    @JsonProperty("additionalFields")
    Map<String,String> additionalFields;

    @JsonProperty("primaryCorrespondent")
    GetCorrespondentResponse primaryCorrespondent;

    @JsonProperty("activeStages")
    Set<ActiveStageDto> activeStages;

    public static GetCaseSummaryResponse from(CaseSummary caseSummary) {
        GetCorrespondentResponse getCorrespondentResponse = null;
        if (caseSummary.getPrimaryCorrespondent() != null) {
            getCorrespondentResponse = GetCorrespondentResponse.from(caseSummary.getPrimaryCorrespondent());
        }
        Set<ActiveStageDto> activeStageDtos = null;
        if (caseSummary.getActiveStages() != null) {
            activeStageDtos = caseSummary.getActiveStages().stream().map(ActiveStageDto::from).collect(Collectors.toSet());
        }
        return new GetCaseSummaryResponse(caseSummary.getCaseDeadline(), caseSummary.getStageDeadlines(), caseSummary.getAdditionalFields(), getCorrespondentResponse, activeStageDtos);
    }
}
