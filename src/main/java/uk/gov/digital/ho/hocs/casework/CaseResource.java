package uk.gov.digital.ho.hocs.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class CaseResource {

    private final CaseService caseService;

    @Autowired
    public CaseResource(CaseService caseService) {
        this.caseService = caseService;
    }

    @RequestMapping(value = "/create/{type}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity createCase(@PathVariable String caseType, @RequestBody CaseDetails caseDetails) {
        log.info("created case \"{}\"", caseType);
        String ref = caseService.create(caseType, caseDetails);
        return ResponseEntity.ok(ref);
    }

}
