package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ActiveStageRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ActiveStageService {

    private final AuditService auditService;
    private final ActiveStageRepository activeStageRepository;
    private final CaseInputDataRepository caseInputDataRepository;



    @Autowired
    public ActiveStageService(ActiveStageRepository activeStageRepository,
                              CaseInputDataRepository caseInputDataRepository,
                              AuditService auditService) {
        this.auditService = auditService;
        this.activeStageRepository = activeStageRepository;
        this.caseInputDataRepository = caseInputDataRepository;
    }

    //TODO: This method is a dev tool
    public Set<ActiveStage> getActiveCases() {
        return activeStageRepository.findAll();
    }

    public Set<ActiveStage> getActiveStagesByUserUUID(UUID userUUID) {
        return activeStageRepository.findAllByUserUUID(userUUID);
    }

    public Set<ActiveStage> getActiveStagesByTeamUUID(Set<UUID> teamUUIDs) {
        return activeStageRepository.findAllByTeamUUIDIn(teamUUIDs);
    }

    public ActiveStage getActiveStageByStageUUID(UUID stageUUID) {
        return activeStageRepository.findByStageUUID(stageUUID);
    }

    @Transactional
    public void allocateStage(StageData stageData) {
        ActiveStage activeStage = getActiveStageByStageUUID(stageData.getUuid());
        if (activeStage != null) {
            updateActiveStage(activeStage, stageData);
        } else {
            addActiveStage(stageData);
        }
    }

    @Transactional
    public void completeStage(UUID stageUUID) {
        log.debug("Completing Stage UUID: {}", stageUUID);
        activeStageRepository.deleteByStageUUID(stageUUID);
        auditService.writeCompleteStageEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    private void addActiveStage(StageData stageData) {
        log.debug("Adding Active Stage UUID {}", stageData.getUuid());
        CaseInputData caseInputData = caseInputDataRepository.findByCaseUUID(stageData.getCaseUUID());
        // TODO: Fake Data.
        ActiveStage activeStage = new ActiveStage(caseInputData, stageData, stageData.getTeamUUID() == null ? "" : stageData.getTeamUUID().toString(), stageData.getUserUUID() == null ? "Unassigned" : stageData.getUserUUID().toString());
        activeStageRepository.save(activeStage);
        log.info("Added Active Stage UUID {}", stageData.getUuid());
    }

    private void updateActiveStage(ActiveStage activeStage, StageData stageData) {
        log.debug("Updating Active Stage UUID {}", activeStage.getStageUUID());
        // TODO: Fake Data.
        activeStage.allocate(stageData.getTeamUUID(),
                stageData.getTeamUUID() == null ? "" : stageData.getTeamUUID().toString(),
                stageData.getUserUUID(),
                stageData.getUserUUID() == null ? "Unassigned" : stageData.getUserUUID().toString());
        activeStageRepository.save(activeStage);
        log.info("Updated Active Stage UUID {}", activeStage.getStageUUID());
    }

}