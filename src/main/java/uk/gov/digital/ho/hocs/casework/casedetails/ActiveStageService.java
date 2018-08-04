package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ActiveStageRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ActiveStageService {

    private final ActiveStageRepository activeStageRepository;

    @Autowired
    public ActiveStageService(ActiveStageRepository activeStageRepository) {
        this.activeStageRepository = activeStageRepository;
    }

    @Transactional
    public void addActiveStage(CaseData caseData, StageData stageData, String assignedTeam, String assignedTeamDisplay, String assignedUser, String assignedUserDisplay) {
        log.debug("Adding Active Stage UUID {}, Case UUID: {} ({})", stageData.getUuid(), caseData.getUuid(), caseData.getReference());
        ActiveStage activeStage = new ActiveStage(caseData, stageData, assignedTeam, assignedTeamDisplay, assignedUser, assignedUserDisplay);
        log.info("Added Active Stage UUID {}, Case UUID: {} ({})", stageData.getUuid(), caseData.getUuid(), caseData.getReference());
        activeStageRepository.save(activeStage);
    }

    //TODO: This method is a dev tool
    public Set<ActiveStage> getActiveCases() {
        return activeStageRepository.findAll();
    }

    @Transactional
    public void removeActiveStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Removing Active Stage UUID {}, Case UUID: {}", stageUUID, caseUUID);
        activeStageRepository.deleteByStageUUID(stageUUID);
        log.info("Removing Active Stage UUID {}, Case UUID: {}", stageUUID, caseUUID);

    }
}
