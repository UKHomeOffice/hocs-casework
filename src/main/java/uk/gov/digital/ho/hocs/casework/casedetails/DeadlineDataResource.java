package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDeadlinesRequest;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class DeadlineDataResource {
    
    private final DeadlineDataService deadlineDataService;

    @Autowired
    public DeadlineDataResource(DeadlineDataService deadlineDataService) {
        this.deadlineDataService = deadlineDataService;
    }

    @PostMapping(value = "/case/{caseUUID}/deadline", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity storeDeadlines(@RequestBody UpdateDeadlinesRequest request, @PathVariable UUID caseUUID) {
        deadlineDataService.updateDeadlines(caseUUID, request.getDeadlines());
        return ResponseEntity.ok().build();
    }
}
