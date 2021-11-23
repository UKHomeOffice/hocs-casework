package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;

@RestController
class DashboardResource {

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DashboardResponse> getDashboard() {
        var dashboardSummaries = dashboardService.getDashboard();
        return ResponseEntity.ok(DashboardResponse.from(dashboardSummaries));
    }

}
