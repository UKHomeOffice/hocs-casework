package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.CaseworkConstants;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class MigrationStageService {

    private final StageRepository stageRepository;
    private final AuditClient auditClient;
    private final MigrationCaseDataService migrationCaseDataService;

    @Autowired
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
        CreateStageRequest createStageRequest = new CreateStageRequest(stageType, null, null, "", null, null);

        Set<Stage> allStages = stageRepository.findAllByCaseUUIDAsStage(caseUUID);
        Set<Stage> activeStages = allStages.stream().filter(stage -> stage.getTeamUUID() != null).collect(Collectors.toSet());
        if (activeStages.size() > 1) {
            log.warn("More than 1 active stage for caseUUID: {}, all active stages will be deactivated", caseUUID);
        }

        Optional<Stage> existingStageOfRequestedType = allStages.stream().filter(stage -> stage.getStageType().equals(createStageRequest.getType())).findFirst();

        final AtomicBoolean isRecreation = new AtomicBoolean(false);
        final AtomicBoolean isRecreateOfCurrentlyActive = new AtomicBoolean(false);
        final AtomicReference<Stage> stageToActivate = new AtomicReference<>(null);

        existingStageOfRequestedType.ifPresentOrElse((Stage stage) -> {
            log.debug("Stage type of {} exists for caseUUID: {}, recreating stage",createStageRequest.getType(), caseUUID);
            stageToActivate.set(stage);
            isRecreation.set(true);
            isRecreateOfCurrentlyActive.set(activeStages.contains(stage));
        },() -> {
            log.debug("Stage type of {} does not currently exist for caseUUID: {}, creating new stage",createStageRequest.getType(), caseUUID);
            stageToActivate.set(new Stage(caseUUID, createStageRequest.getType(),
                    null, null, createStageRequest.getTransitionNoteUUID()));
        });

        CaseData caseData = migrationCaseDataService.getCaseData(caseUUID);


        stageRepository.save(stageToActivate.get());
        log.info("Created Stage: {}, Type: {}, Case: {}", stageToActivate.get().getUuid(), stageToActivate.get().getStageType(), stageToActivate.get().getCaseUUID(), value(EVENT, STAGE_CREATED));
        caseData.setCompleted(true);
        migrationCaseDataService.updateCaseData(caseData, stageToActivate.get().getUuid(), Map.of(CaseworkConstants.CURRENT_STAGE, stageToActivate.get().getStageType()));

        // Update audit and timeline unless active and allocated to same user and team - positioned here maintains timeline consistency.
        if (isRecreation.get() && !isRecreateOfCurrentlyActive.get()) {
            auditClient.recreateStage(stageToActivate.get());
        } else if (!isRecreation.get()) {
            auditClient.createStage(stageToActivate.get());
        }

        updateAssignmentAudit(stageToActivate.get());

        return stageToActivate.get();
    }

    private void updateAssignmentAudit(BaseStage stage) {
        auditClient.updateStageTeam(stage);
        if (stage.getUserUUID() != null) {
            auditClient.updateStageUser(stage);
        }
    }

}
