package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.model.UnitType;

import java.time.LocalDate;

@RestController
class CaseAuditResource {

    private final CaseAuditService caseAuditService;

    @Autowired
    public CaseAuditResource(CaseAuditService caseAuditService) {
        this.caseAuditService = caseAuditService;
    }

    @GetMapping(value = "/report/{unit}/current", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCurrent(@PathVariable("unit") UnitType unit) {
        String value = caseAuditService.getReportingDataAsCSV(unit, LocalDate.now());
        return ResponseEntity.ok(value);
    }

    @GetMapping(value = "/report/{unit}/{cutoff}", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCutoff(@PathVariable("unit") UnitType unit, @PathVariable("cutoff") String cutoff) {
        LocalDate cutoffDate = LocalDate.parse(cutoff);
        String value = caseAuditService.getReportingDataAsCSV(unit, cutoffDate);
        return ResponseEntity.ok(value);
    }

}
