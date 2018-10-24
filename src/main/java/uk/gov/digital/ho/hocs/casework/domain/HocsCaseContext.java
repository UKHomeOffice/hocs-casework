package uk.gov.digital.ho.hocs.casework.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.StageService;

@Component
@Getter
public class HocsCaseContext {

    private final CaseDataService caseDataService;
    private final CaseNoteService caseNoteService;
    private final CorrespondentService correspondentService;
    private final StageService stageService;

    public HocsCaseContext(CaseDataService caseDataService,
                           CaseNoteService caseNoteService,
                           CorrespondentService correspondentService,
                           StageService stageService) {

        this.caseDataService = caseDataService;
        this.caseNoteService = caseNoteService;
        this.correspondentService = correspondentService;
        this.stageService = stageService;
    }
}
