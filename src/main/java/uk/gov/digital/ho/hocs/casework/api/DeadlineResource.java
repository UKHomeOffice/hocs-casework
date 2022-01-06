package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class DeadlineResource {
    DeadlineService deadlineService;

    @Autowired
    public DeadlineResource(DeadlineService deadlineService) {
        this.deadlineService = deadlineService;
    }

    @GetMapping(value = "/deadline/{caseType}", params = {"received","days"}, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<LocalDate> getCaseDeadlineOverriddenSla(@PathVariable final String caseType,
                                              @RequestParam final String received,
                                              @RequestParam final int days) {

        LocalDate receivedDate = LocalDate.parse(received);
        return ResponseEntity.ok(deadlineService.calculateWorkingDaysForCaseType(caseType, receivedDate, days));
    }
}
