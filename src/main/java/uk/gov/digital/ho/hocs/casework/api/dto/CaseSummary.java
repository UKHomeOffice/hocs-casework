package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseSummary {

    @JsonProperty("caseDeadline")
    LocalDate caseDeadline;

    @JsonProperty("stageDeadlines")
    Map<String, LocalDate> stageDeadlines;

    @JsonProperty("additionalFields")
    Map<String,String> additionalFields;

    @JsonProperty("primaryCorrespondent")
    CorrespondentDto primaryCorrespondent;

    @JsonProperty("activeStages")
    Set<ActiveStage> activeStages;

    public static CaseSummary from(LocalDate caseDeadline, Map<String, LocalDate> stageDeadlines, Map<String, String> additionalFields, Correspondent correspondent, Set<Stage> stages) {
        CorrespondentDto correspondentDto = null;
        if (correspondent != null) {
            CorrespondentDto.from(correspondent);
        }
        Set<ActiveStage> activeStages = null;
        if (stages != null) {
            activeStages = stages.stream().map(ActiveStage::from).collect(Collectors.toSet());
        }
        return new CaseSummary(caseDeadline, stageDeadlines, additionalFields, correspondentDto, activeStages);
    }
}
