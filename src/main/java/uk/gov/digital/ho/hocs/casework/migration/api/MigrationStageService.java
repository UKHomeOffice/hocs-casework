package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.ActionDataDeadlineExtensionService;
import uk.gov.digital.ho.hocs.casework.api.CaseworkConstants;
import uk.gov.digital.ho.hocs.casework.api.DeadlineService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.time.LocalDate;
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
    private final NotifyClient notifyClient;
    private final AuditClient auditClient;
    private final InfoClient infoClient;
    private final MigrationCaseDataService migrationCaseDataService;
    private final ActionDataDeadlineExtensionService extensionService;
    private final DeadlineService deadlineService;

    @Autowired
    public MigrationStageService(StageRepository stageRepository,
                                 NotifyClient notifyClient,
                                 AuditClient auditClient,
                                 InfoClient infoClient,
                                 MigrationCaseDataService migrationCaseDataService,
                                 ActionDataDeadlineExtensionService extensionService,
                                 DeadlineService deadlineService) {
        this.stageRepository = stageRepository;
        this.notifyClient = notifyClient;
        this.auditClient = auditClient;
        this.infoClient = infoClient;
        this.migrationCaseDataService = migrationCaseDataService;
        this.extensionService = extensionService;
        this.deadlineService = deadlineService;
    }

    @Transactional
    public Stage createStage(UUID caseUUID, CreateStageRequest createStageRequest) {

        log.debug("Creating Stage: {} for case {}", createStageRequest, caseUUID);

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
        calculateDeadlines(stageToActivate.get(), caseData);

        stageRepository.save(stageToActivate.get());
        log.info("Created Stage: {}, Type: {}, Case: {}", stageToActivate.get().getUuid(), stageToActivate.get().getStageType(), stageToActivate.get().getCaseUUID(), value(EVENT, STAGE_CREATED));

        migrationCaseDataService.updateCaseData(caseData, stageToActivate.get().getUuid(), Map.of(CaseworkConstants.CURRENT_STAGE, stageToActivate.get().getStageType()));

        List<UUID> assignedUserUUIDList = activeStages.stream().map(BaseStage::getUserUUID).collect(Collectors.toList());
        UUID currentUserUUID = null;

        if (!assignedUserUUIDList.isEmpty()) {
            currentUserUUID = assignedUserUUIDList.get(0);
        }

//        assignTeamAndMemberUserToStage(stageToActivate.get(), createStageRequest.getTeamUUID(), createStageRequest.getUserUUID());

        // deactivate old active stage/s if new stage not current stage
        if (!isRecreateOfCurrentlyActive.get()) {
            activeStages.forEach(value -> updateStageTeam(value, caseData.getDataMap(), caseData.getReference(), null, null));
        }

        // Update audit and timeline unless active and allocated to same user and team - positioned here maintains timeline consistency.
        if (isRecreation.get() && !isRecreateOfCurrentlyActive.get()) {
            auditClient.recreateStage(stageToActivate.get());
        } else if (!isRecreation.get()) {
            auditClient.createStage(stageToActivate.get());
        }

        updateAssignmentAudit(stageToActivate.get());

//        sendAssignmentNotifications(caseData, stageToActivate.get(), currentUserUUID);
        return stageToActivate.get();
    }

    private void updateAssignmentAudit(BaseStage stage) {
        auditClient.updateStageTeam(stage);
        if (stage.getUserUUID() != null) {
            auditClient.updateStageUser(stage);
        }
    }

    private void calculateDeadlines(Stage stage, CaseData caseData) {
        log.debug("Updating Stage Deadline; Case: {}, Stage: {}", stage.getCaseUUID(), stage.getUuid());
        // Try and overwrite the deadline with inputted values from the data map.
        var overrideDeadline = caseData.getData(String.format("%s_DEADLINE", stage.getStageType()));

        final StageTypeDto stageDefinition = infoClient.getAllStagesForCaseType(caseData.getType()).stream()
                .filter(element -> element.getType().equals(stage.getStageType())).collect(Collectors.toList()).get(0);

        if (overrideDeadline == null) {
            LocalDate deadline =
                    deadlineService.calculateWorkingDaysForStage(
                            caseData.getType(),
                            caseData.getDateReceived(),
                            caseData.getCaseDeadline(),
                            stageDefinition.getSla());
            stage.setDeadline(deadline);
            if (caseData.getCaseDeadlineWarning() != null) {
                stage.setDeadlineWarning(caseData.getCaseDeadlineWarning());
            }
        }

        boolean isExtended = extensionService.hasExtensions(caseData.getUuid());
        if (isExtended) {
            stage.setDeadline(caseData.getCaseDeadline());
            if (caseData.getCaseDeadlineWarning() != null) {
                LocalDate deadlineWarning = caseData.getCaseDeadlineWarning();
                stage.setDeadlineWarning(deadlineWarning);
            }
        }

        if (overrideDeadline != null) {
            LocalDate deadline = LocalDate.parse(overrideDeadline);
            if (stage.getDeadline() == null || stage.getDeadline().isBefore(deadline)) {
                stage.setDeadline(deadline);
                stage.setDeadlineWarning(null);
            }
        }
        log.info("Stage Deadline Updated; Case: {}, Stage: {}", stage.getCaseUUID(), stage.getUuid());
    }

    private void updateStageTeam(BaseStage stage, Map<String, String> caseDataMap, String caseReference, UUID newTeamUUID, String emailType) {
        stage.setTeam(newTeamUUID);
//        checkSendOfflineQAEmail(stage, caseDataMap, caseReference);
        stageRepository.save(stage);
        auditClient.updateStageTeam(stage);
        if (newTeamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stage.getUuid(), stage.getCaseUUID(), value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stage.getUuid(), newTeamUUID, stage.getCaseUUID(), value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(stage.getCaseUUID(), stage.getUuid(), newTeamUUID, caseReference, emailType);
        }
    }

}
