package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CaseSummary {

    LocalDate createdDate;

    LocalDate caseDeadline;

    Map<String, LocalDate> stageDeadlines;

    Set<AdditionalField> additionalFields;

    Correspondent primaryCorrespondent;

    Topic primaryTopic;

    Set<ActiveStage> activeStages;

    String previousCaseReference;

    UUID previousCaseUUID;

    UUID previousCaseStageUUID;

}