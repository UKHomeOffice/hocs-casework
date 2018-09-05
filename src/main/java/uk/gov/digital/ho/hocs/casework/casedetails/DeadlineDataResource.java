package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DeadlineDataResource {

    private final DeadlineDataService deadlineDataService;

    @Autowired
    public DeadlineDataResource(DeadlineDataService deadlineDataService) {
        this.deadlineDataService = deadlineDataService;
    }


}
