package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseSummaryResponse {

    @JsonProperty("caseCreated")
    LocalDate caseCreated;

    @JsonProperty("caseDeadline")
    LocalDate caseDeadline;

    @JsonProperty("stageDeadlines")
    Map<String, LocalDate> stageDeadlines;

    @JsonProperty("additionalFields")
    List<AdditionalFieldDto> additionalFields;

    @JsonProperty("primaryCorrespondent")
    GetCorrespondentResponse primaryCorrespondent;

    @JsonProperty("primaryTopic")
    GetTopicResponse primaryTopic;

    @JsonProperty("activeStages")
    Set<ActiveStageDto> activeStages;



    public static GetCaseSummaryResponse from(CaseSummary caseSummary) {
        GetCorrespondentResponse getCorrespondentResponse = null;
        if (caseSummary.getPrimaryCorrespondent() != null) {
            getCorrespondentResponse = GetCorrespondentResponse.from(caseSummary.getPrimaryCorrespondent());
        }

        GetTopicResponse getTopicsResponse = null;
        if (caseSummary.getPrimaryTopic() != null) {
            getTopicsResponse = GetTopicResponse.from(caseSummary.getPrimaryTopic());
        }

        Set<ActiveStageDto> activeStageDtos = new HashSet<>();
        if (caseSummary.getActiveStages() != null) {
            activeStageDtos.addAll(caseSummary.getActiveStages().stream().map(ActiveStageDto::from).collect(Collectors.toSet()));
        }

        List<AdditionalFieldDto> additionalFieldDtos = new ArrayList<>();
        if (caseSummary.getAdditionalFields() != null) {
            additionalFieldDtos.addAll(caseSummary.getAdditionalFields().stream().filter(field -> !field.getValue().equals("")).map(AdditionalFieldDto::from).collect(Collectors.toList()));
        }
        additionalFieldDtos.sort(Comparator.comparing(AdditionalFieldDto::getLabel));
        return new GetCaseSummaryResponse(caseSummary.getCreatedDate(), caseSummary.getCaseDeadline(), caseSummary.getStageDeadlines(), additionalFieldDtos, getCorrespondentResponse, getTopicsResponse, activeStageDtos);
    }
}
