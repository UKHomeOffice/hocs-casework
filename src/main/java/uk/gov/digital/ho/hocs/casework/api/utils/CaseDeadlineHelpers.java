package uk.gov.digital.ho.hocs.casework.api.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;

@Slf4j
public class CaseDeadlineHelpers {
    private CaseDeadlineHelpers() {}

    public static void overrideStageDeadlines(CaseData caseData) {

        if (caseData.getActiveStages() == null) {
            log.warn("Case uuid:{} supplied with null active stages", caseData.getUuid());
            return;
        }

        for (ActiveStage stage : caseData.getActiveStages()) {
            LocalDate caseDeadlineWarning = caseData.getCaseDeadlineWarning();
            stage.setDeadline(caseData.getCaseDeadline());
            if (caseDeadlineWarning != null) {
                stage.setDeadlineWarning(caseDeadlineWarning);
            }
        }
    }
}
