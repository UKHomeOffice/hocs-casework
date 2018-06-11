package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.audit.model.ExportLine;

import java.time.LocalDate;
import java.util.List;

@RestController
class CaseDataAuditResource {

    private final CaseDataAuditService caseDataAuditService;

    @Autowired
    public CaseDataAuditResource(CaseDataAuditService caseDataAuditService) {
        this.caseDataAuditService = caseDataAuditService;
    }

    @RequestMapping(value = "/report/{unit}/current", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCurrent(@PathVariable("unit") String unit, @RequestHeader("X-Auth-Username") String username) {
        String value = caseDataAuditService.getReportingDataAsCSV(unit, LocalDate.now(), username);
        return ResponseEntity.ok(value);
    }

    @RequestMapping(value = "/report/{unit}/current/json", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ExportLine>> getReportCurrentJson(@PathVariable("unit") String unit, @RequestHeader("X-Auth-Username") String username) {
        List<ExportLine> value = caseDataAuditService.getReportingDataAsJson(unit, LocalDate.now(), username);
        return ResponseEntity.ok(value);
    }

    @RequestMapping(value = "/report/{unit}/{cutoff}", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> getReportCutoff(@PathVariable("unit") String unit, @PathVariable("cutoff") String cutoff, @RequestHeader("X-Auth-Username") String username) {
        LocalDate cutoffDate = LocalDate.parse(cutoff);
        String value = caseDataAuditService.getReportingDataAsCSV(unit, cutoffDate, username);
        return ResponseEntity.ok(value);
    }

    @RequestMapping(value = "/report/{unit}/{cutoff}/json", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ExportLine>> getReportCutoffJson(@PathVariable("unit") String unit, @PathVariable("cutoff") String cutoff, @RequestHeader("X-Auth-Username") String username) {
        LocalDate cutoffDate = LocalDate.parse(cutoff);
        List<ExportLine> value = caseDataAuditService.getReportingDataAsJson(unit, cutoffDate, username);
        return ResponseEntity.ok(value);
    }
}
