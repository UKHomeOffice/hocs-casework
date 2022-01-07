package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate received,
                                              @RequestParam final int days) {

        return ResponseEntity.ok(deadlineService.calculateWorkingDaysForCaseType(caseType, received, days));
    }
}
