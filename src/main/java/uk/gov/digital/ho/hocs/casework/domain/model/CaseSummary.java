package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;

import java.time.LocalDate;
import java.util.*;

@AllArgsConstructor
@Getter
public class CaseSummary {

    private final String type;
    private final LocalDate createdDate;
    private final LocalDate caseDeadline;
    private final Map<String, LocalDate> stageDeadlines;
    private final Set<AdditionalField> additionalFields;
    private final Correspondent primaryCorrespondent;
    private final Topic primaryTopic;
    private final Set<ActiveStage> activeStages;
    private final String previousCaseReference;
    private final UUID previousCaseUUID;
    private final UUID previousCaseStageUUID;
    private final CaseActionDataResponseDto actions;
    private final String suspended;

    public static class Builder {
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

        public Builder withCaseType(final String type) {
            this.type = type;

            return this;
        }

        public Builder withCreatedDate(final LocalDate createdDate) {
            this.createdDate = createdDate;

            return this;
        }

        public Builder withCaseDeadline(final LocalDate caseDeadline) {
            this.caseDeadline = caseDeadline;

            return this;
        }

        public Builder withStageDeadlines(final Map<String, LocalDate> stageDeadlines) {
            this.stageDeadlines = stageDeadlines;

            return this;
        }

        public Builder withAdditionalFields(final Set<AdditionalField> additionalFields) {
            this.additionalFields = additionalFields;

            return this;
        }

        public Builder withPrimaryCorrespondent(final Correspondent primaryCorrespondent) {
            this.primaryCorrespondent = primaryCorrespondent;

            return this;
        }

        public Builder withPrimaryTopic(final Topic primaryTopic) {
            this.primaryTopic = primaryTopic;

            return this;
        }

        public Builder withActiveStages(final Set<ActiveStage> activeStages) {
            this.activeStages = activeStages;

            return this;
        }

        public Builder withPreviousCaseReference(String previousCaseReference) {
            this.previousCaseReference = previousCaseReference;

            return this;
        }

        public Builder withPreviousCaseUUID(UUID previousCaseUUID) {
            this.previousCaseUUID = previousCaseUUID;

            return this;
        }

        public Builder withPreviousCaseStageUUID(UUID previousCaseStageUUID) {
            this.previousCaseStageUUID = previousCaseStageUUID;

            return this;
        }

        public Builder withActions(CaseActionDataResponseDto actions) {
            this.actions = actions;

            return this;
        }

        public Builder withSuspended(String suspended) {
            this.suspended = suspended;
            return this;
        }

        public CaseSummary build() {
            return new CaseSummary(
                    type,
                    createdDate,
                    caseDeadline,
                    stageDeadlines,
                    additionalFields,
                    primaryCorrespondent,
                    primaryTopic,
                    activeStages,
                    previousCaseReference,
                    previousCaseUUID,
                    previousCaseStageUUID,
                    actions,
                    suspended
            );
        }
    }


}