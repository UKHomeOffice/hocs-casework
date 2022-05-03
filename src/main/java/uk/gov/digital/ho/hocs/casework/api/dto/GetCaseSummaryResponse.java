package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.ObjectUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GetCaseSummaryResponse {
    @JsonProperty("type")
    String type;

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

    @JsonProperty("previousCase")
    private CaseSummaryLink previousCase;

    @JsonProperty("actions")
    private CaseActionDataResponseDto actions;

    private final String suspended;

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
            additionalFieldDtos.addAll(caseSummary.getAdditionalFields().stream()
                    .filter(field -> !ObjectUtils.isEmpty(field.getValue()))
                    .map(AdditionalFieldDto::from)
                    .collect(Collectors.toList()));
        }
        additionalFieldDtos.sort(Comparator.comparing(AdditionalFieldDto::getLabel));

        return new GetCaseSummaryResponse(
                caseSummary.getType(),
                caseSummary.getCreatedDate(),
                caseSummary.getCaseDeadline(),
                caseSummary.getStageDeadlines(),
                additionalFieldDtos,
                getCorrespondentResponse,
                getTopicsResponse,
                activeStageDtos,
                CaseSummaryLink.builder()
                        .caseUUID(caseSummary.getPreviousCaseUUID())
                        .caseReference(caseSummary.getPreviousCaseReference())
                        .stageUUID(caseSummary.getPreviousCaseStageUUID()).build(),
                caseSummary.getActions(),
                caseSummary.getSuspended()
        );


    }

    protected void replaceAdditionalFields(List<AdditionalFieldDto> additionalFieldDtoList) {
        this.additionalFields = additionalFieldDtoList;
    }

    protected void replaceActiveStages(Set<ActiveStageDto> activeStageSet) {
        this.activeStages = activeStageSet;
    }

    protected void clearCaseActionData() {

        if (this.actions != null) {
            Map<String, List<ActionDataDto>> emptyCaseActionDataMap = new HashMap<>();
            this.actions = CaseActionDataResponseDto.from(
                    emptyCaseActionDataMap,
                    this.actions.getCaseTypeActionData(),
                    this.actions.getCurrentCaseDeadline(),
                    this.actions.getRemainingDaysUntilDeadline());
        }
    }

}