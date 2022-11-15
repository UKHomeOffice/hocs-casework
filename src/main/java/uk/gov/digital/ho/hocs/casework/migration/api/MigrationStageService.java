package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;

@Slf4j
@Service
public class MigrationStageService {

    private final StageRepository stageRepository;

    public MigrationStageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public BaseStage createStageForClosedCase(UUID caseUUID, String stageType) {
        log.debug("Creating Stage: {} for case {}", stageType, caseUUID);
        BaseStage stage = new BaseStage(caseUUID, stageType, null, null, null);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stageType, stage.getCaseUUID(),
            value(EVENT, STAGE_CREATED));
        return stage;
    }

}
