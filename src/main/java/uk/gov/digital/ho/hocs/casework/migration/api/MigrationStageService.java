package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;

@Slf4j
@Service
public class MigrationStageService {

    private static final Set<String> MIGRATION_CLOSED_STATES = Set.of("MIGRATION_COMP_CASE_CLOSED",
        "MIGRATION_BF_CASE_CLOSED", "MIGRATION_IEDET_CASE_CLOSED", "MIGRATION_POGR_CASE_CLOSED",
        "MIGRATION_TO_CASE_CLOSED"
                                                                     );

    private final StageRepository stageRepository;

    public MigrationStageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public Stage createStageForClosedCase(UUID caseUUID, String stageType) {
        log.debug("Creating Stage: {} for case {}", stageType, caseUUID);
        Stage stage = new Stage(caseUUID, stageType, null, null, null);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stageType, stage.getCaseUUID(),
            value(EVENT, STAGE_CREATED)
                );
        return stage;
    }

    public Optional<Stage> getStageForCaseUUID(UUID caseUUID) {
        log.debug("Getting stage for case {}", caseUUID);
        return stageRepository
            .findAllByCaseUUIDAsStage(caseUUID)
            .stream()
            .filter(stage -> stage.getTeamUUID() != null || MIGRATION_CLOSED_STATES.contains(stage.getStageType()))
            .findFirst();
    }

}
