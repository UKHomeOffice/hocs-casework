package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.CaseworkConstants;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;

@Slf4j
@Service
public class MigrationStageService {

    private final StageRepository stageRepository;
    private final AuditClient auditClient;
    private final MigrationCaseDataService migrationCaseDataService;

    public MigrationStageService(StageRepository stageRepository,
                                 AuditClient auditClient,
                                 MigrationCaseDataService migrationCaseDataService) {
        this.stageRepository = stageRepository;
        this.auditClient = auditClient;
        this.migrationCaseDataService = migrationCaseDataService;
    }

    @Transactional
    public Stage createStageForClosedCase(UUID caseUUID, String stageType) {
        log.debug("Creating Stage: {} for case {}", stageType, caseUUID);
        Stage stage = new Stage(caseUUID, stageType, null, null, null);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stageType, stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        return stage;
    }

}
