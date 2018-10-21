package uk.gov.digital.ho.hocs.casework.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.casedetails.*;

@Component
@Getter
public class HocsCaseContext {

    private final CaseDataService caseDataService;
    private final CaseNoteService caseNoteService;
    private final CorrespondentService correspondentService;
    private final ReferenceDataService referenceDataService;
    private final StageDataService stageDataService;

    public HocsCaseContext(CaseDataService caseDataService,
                           CaseNoteService caseNoteService,
                           CorrespondentService correspondentService,
                           ReferenceDataService referenceDataService,
                           StageDataService stageDataService) {

        this.caseDataService = caseDataService;
        this.caseNoteService = caseNoteService;
        this.correspondentService = correspondentService;
        this.referenceDataService = referenceDataService;
        this.stageDataService = stageDataService;
    }
}
