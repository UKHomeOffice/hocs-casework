package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class RshCaseResource {

    private final RshCaseService rshCaseService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService) {
        this.rshCaseService = rshCaseService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity rshCreateCase( @RequestBody RshCaseDetails rshCaseDetails) {
        log.info("creating case - rsh");
        String ref = rshCaseService.rshCreate("rsh", rshCaseDetails);
        log.info("created case \"{}\"", ref);
        return ResponseEntity.ok(ref);
    }

    @RequestMapping(value = "/rsh/{uuid}/update", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity rshUpdateCase(@PathVariable UUID uuid, @RequestBody RshCaseDetails rshCaseDetails) {
        log.info("updating case \"{}\"", uuid);
        rshCaseService.rshUpdate(uuid,rshCaseDetails);
        return ResponseEntity.ok(uuid);
    }
}
