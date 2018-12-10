package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CaseSummary {
    LocalDate caseDeadline;
    Map<String, LocalDate> stageDeadlines;
    Map<String,String> additionalFields;
    CorrespondentDto primaryCorrespondent;
    Set<ActiveStage> activeStages;
}
