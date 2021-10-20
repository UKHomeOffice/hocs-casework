package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.SomuTypeDto;

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
    private final Map<String, Integer> deadLineExtensions;
    private final String previousCaseReference;
    private final UUID previousCaseUUID;
    private final UUID previousCaseStageUUID;
    private final Collection<CaseSummarySomuItems> somuItems;
    private final Map<String, List<ActionDataDto>> actions;

    public static class Builder {
        private String type;
        private LocalDate createdDate;
        private LocalDate caseDeadline;
        private Map<String, LocalDate> stageDeadlines;
        private Set<AdditionalField> additionalFields;
        private Correspondent primaryCorrespondent;
        private Topic primaryTopic;
        private Set<ActiveStage> activeStages;
        private Map<String, Integer> deadLineExtensions;
        private String previousCaseReference;
        private UUID previousCaseUUID;
        private UUID previousCaseStageUUID;
        private Map<UUID, CaseSummarySomuItems> somuItems = new HashMap<>();
        private Map<String, List<ActionDataDto>> actions = new HashMap<>();

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

        public Builder withDeadlineExtensions(final Map<String, Integer> deadLineExtensions) {
            this.deadLineExtensions = deadLineExtensions;

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

        public Builder addSomuItem(SomuTypeDto somuType, String somuItemData) throws JsonProcessingException {
            if(!somuItems.containsKey(somuType.getUuid())) {
                somuItems.put(somuType.getUuid(), new CaseSummarySomuItems(somuType.getSchema()));
            }

            somuItems.get(somuType.getUuid()).addItem(somuItemData);

            return this;
        }

        public Builder withActions(Map<String, List<ActionDataDto>> actions) {
            this.actions = actions;

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
                    deadLineExtensions,
                    previousCaseReference,
                    previousCaseUUID,
                    previousCaseStageUUID,
                    somuItems.values(),
                    actions
            );
        }
    }


}