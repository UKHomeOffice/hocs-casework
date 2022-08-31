package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

@RestController
@Slf4j
public class CaseMigrationResource {

    private final CaseMigrationService caseMigrationService;
    private final StageService stageService;


    public CaseMigrationResource(CaseMigrationService caseMigrationService, StageService stageService) {
        this.caseMigrationService = caseMigrationService;
        this.stageService = stageService;
    }

    @PostMapping(value = "/migrate")
    public ResponseEntity<CreateCaseResponse> createMigrationCase(@RequestBody CreateMigrationCaseRequest request) {
        CaseData caseData = caseMigrationService.createMigrationCase(request.getType(), request.getStageType(), request.getData(), request.getDateReceived());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }
}
