package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

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
    public ResponseEntity<CreateCaseResponse> createMigrationCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseMigrationService.createMigrationCase(request.getType(), request.getData(), request.getDateReceived());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }
}
