package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.UnitType;

import java.time.LocalDate;

@RestController
class CaseDataAuditResource {

    private final CaseDataAuditService caseDataAuditService;

    @Autowired
    public CaseDataAuditResource(CaseDataAuditService caseDataAuditService) {
        this.caseDataAuditService = caseDataAuditService;
    }

    @RequestMapping(value = "/report/{unit}/current", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCurrent(@PathVariable("unit") UnitType unit) {
        String value = caseDataAuditService.getReportingDataAsCSV(unit, LocalDate.now());
        return ResponseEntity.ok(value);
    }

    @RequestMapping(value = "/report/{unit}/{cutoff}", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCutoff(@PathVariable("unit") UnitType unit, @PathVariable("cutoff") String cutoff) {
        LocalDate cutoffDate = LocalDate.parse(cutoff);
        String value = caseDataAuditService.getReportingDataAsCSV(unit, cutoffDate);
        return ResponseEntity.ok(value);
    }

}
