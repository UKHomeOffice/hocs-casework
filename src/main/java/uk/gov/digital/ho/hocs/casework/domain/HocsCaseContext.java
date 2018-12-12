package uk.gov.digital.ho.hocs.casework.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.api.StageService;

@Component
@Getter
public class HocsCaseContext {

    private final CaseDataService caseDataService;
    private final CaseNoteService caseNoteService;
    private final StageService stageService;

    public HocsCaseContext(CaseDataService caseDataService,
                           CaseNoteService caseNoteService,
                           StageService stageService) {

        this.caseDataService = caseDataService;
        this.caseNoteService = caseNoteService;
        this.stageService = stageService;
    }
}
