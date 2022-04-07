package uk.gov.digital.ho.hocs.casework.api.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class ActionDataHelpers {
    private ActionDataHelpers() {}

    // COPIED FROM CaseDataService to avoid cyclic dependency.
    public static void updateStageDeadlines(CaseData caseData) {

        if (caseData.getActiveStages() == null) {
            log.warn("Case uuid:{} supplied with null active stages", caseData.getUuid());
            return;
        }

        Map<String, String> dataMap = caseData.getDataMap();
        for (ActiveStage stage : caseData.getActiveStages()) {
            // Try and overwrite the deadlines with inputted values from the data map.
            String overrideDeadline = dataMap.get(String.format("%s_DEADLINE", stage.getStageType()));
            if (overrideDeadline == null) {

                LocalDate caseDeadlineWarning = caseData.getCaseDeadlineWarning();
                stage.setDeadline(caseData.getCaseDeadline());
                if (caseDeadlineWarning != null) {
                    stage.setDeadlineWarning(caseDeadlineWarning);
                }
            } else {
                LocalDate deadline = LocalDate.parse(overrideDeadline);
                stage.setDeadline(deadline);
            }

        }
    }
}
