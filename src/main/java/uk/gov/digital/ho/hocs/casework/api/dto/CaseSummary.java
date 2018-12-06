package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CaseSummary {
    LocalDate caseDeadline;
    Map<StageType, LocalDate> stageDeadlines;
    Map<String,String> additionalFields;
    Correspondent primaryCorrespondent;
    Set<ActiveStage> activeStages;
}
