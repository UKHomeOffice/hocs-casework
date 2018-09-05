package uk.gov.digital.ho.hocs.casework.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.casedetails.*;

@Component
@Getter
public class HocsCaseContext {

    private final CaseDataService caseDataService;
    private final CorrespondentDataService correspondentDataService;
    private final DeadlineDataService deadlineDataService;
    private final DocumentDataService documentDataService;
    private final InputDataService inputDataService;
    private final ReferenceDataService referenceDataService;
    private final StageDataService stageDataService;

    public HocsCaseContext(CaseDataService caseDataService,
                           CorrespondentDataService correspondentDataService,
                           DeadlineDataService deadlineDataService,
                           DocumentDataService documentDataService,
                           InputDataService inputDataService,
                           ReferenceDataService referenceDataService,
                           StageDataService stageDataService) {

        this.caseDataService = caseDataService;
        this.correspondentDataService = correspondentDataService;
        this.deadlineDataService = deadlineDataService;
        this.documentDataService = documentDataService;
        this.inputDataService = inputDataService;
        this.referenceDataService = referenceDataService;
        this.stageDataService = stageDataService;
    }
}
