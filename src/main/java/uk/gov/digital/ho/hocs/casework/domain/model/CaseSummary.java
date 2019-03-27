package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
public class CaseSummary {

    LocalDate caseDeadline;

    Map<String, LocalDate> stageDeadlines;

    Set<AdditionalField> additionalFields;

    Correspondent primaryCorrespondent;

    Topic primaryTopic;

    Set<ActiveStage> activeStages;
}