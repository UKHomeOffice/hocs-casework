package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final RshCaseService rshCaseService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService) {
        this.rshCaseService = rshCaseService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<String>> rshCreateCase(@RequestBody String data) {
        String ref = rshCaseService.createRSHCase(data);
        return Mono.justOrEmpty(ResponseEntity.ok(ref));
    }

    @RequestMapping(value = "/rsh/update/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity> rshUpdateCase(@PathVariable String uuid, @RequestBody String data) {
         rshCaseService.updateRSHCase(uuid,data);
        return Mono.justOrEmpty(ResponseEntity.ok().build());
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<RshCaseDetails>> rshGetCase(@PathVariable String uuid) {
        RshCaseDetails caseDetails = rshCaseService.getRSHCase(uuid);
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

  //  @RequestMapping(value = "/rsh/search/", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  //  public Mono<ResponseEntity<CaseSummaryResponse[]>> rshFindCases(@RequestBody String data) {
  //      RshCaseDetails[] caseDetails = rshCaseService.findCases(data);
  //      return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
  //  }
}
