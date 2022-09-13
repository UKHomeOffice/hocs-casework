package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;

import java.time.LocalDate;
import java.util.*;

@AllArgsConstructor
@Getter
public class CaseSummary {

    private String type;

    private LocalDate createdDate;

    private LocalDate caseDeadline;

    private Map<String, LocalDate> stageDeadlines;

    private Set<AdditionalField> additionalFields;

    private Correspondent primaryCorrespondent;

    private Topic primaryTopic;

    private Set<ActiveStage> activeStages;

    private String previousCaseReference;

    private UUID previousCaseUUID;

    private UUID previousCaseStageUUID;

    private CaseActionDataResponseDto actions;

    private String suspended;

    public CaseSummary(CaseData caseData) {
        this.type = caseData.getType();
        this.createdDate = caseData.getCreated().toLocalDate();
        this.caseDeadline = caseData.getCaseDeadline();
        this.primaryCorrespondent = caseData.getPrimaryCorrespondent();
        this.activeStages = caseData.getActiveStages();
    }

    public CaseSummary withStageDeadlines(final Map<String, LocalDate> stageDeadlines) {
        this.stageDeadlines = stageDeadlines;

        return this;
    }

    public CaseSummary withAdditionalFields(final Set<AdditionalField> additionalFields) {
        this.additionalFields = additionalFields;

        return this;
    }

    public CaseSummary withPrimaryTopic(final Topic primaryTopic) {
        this.primaryTopic = primaryTopic;

        return this;
    }

    public CaseSummary withPreviousCaseReference(String previousCaseReference) {
        this.previousCaseReference = previousCaseReference;

        return this;
    }

    public CaseSummary withPreviousCaseUUID(UUID previousCaseUUID) {
        this.previousCaseUUID = previousCaseUUID;

        return this;
    }

    public CaseSummary withPreviousCaseStageUUID(UUID previousCaseStageUUID) {
        this.previousCaseStageUUID = previousCaseStageUUID;

        return this;
    }

    public CaseSummary withActions(CaseActionDataResponseDto actions) {
        this.actions = actions;

        return this;
    }

    public CaseSummary withSuspended(String suspended) {
        this.suspended = suspended;
        return this;
    }

}
