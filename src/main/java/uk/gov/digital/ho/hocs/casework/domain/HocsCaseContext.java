package uk.gov.digital.ho.hocs.casework.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.*;

@Component
@Getter
public class HocsCaseContext {

    private final CaseDataService caseDataService;
    private final CaseNoteService caseNoteService;
    private final CorrespondentService correspondentService;
    private final StageService stageService;
    private final TopicService topicService;

    public HocsCaseContext(CaseDataService caseDataService,
                           CaseNoteService caseNoteService,
                           CorrespondentService correspondentService,
                           StageService stageService,
                           TopicService topicService) {

        this.caseDataService = caseDataService;
        this.caseNoteService = caseNoteService;
        this.correspondentService = correspondentService;
        this.stageService = stageService;
        this.topicService = topicService;
    }
}
