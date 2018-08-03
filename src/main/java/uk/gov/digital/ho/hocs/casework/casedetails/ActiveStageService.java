package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ActiveStageRepository;

import javax.transaction.Transactional;
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
    public void addActiveStage(UUID caseUUID, UUID stageUUID, String caseReference, CaseType caseType, StageType stageType, String assignedTeam, String assignedTeamDisplay, String assignedUser, String assignedUserDisplay) {
        ActiveStage activeStage = new ActiveStage(caseUUID, stageUUID, caseReference, caseType, stageType, assignedTeam, assignedTeamDisplay, assignedUser, assignedUserDisplay);
        activeStageRepository.save(activeStage);
    }

    @Transactional
    public void removeActiveStage(UUID stageUUID) {
        activeStageRepository.deleteByStageUUID(stageUUID);
    }

    public Iterable<ActiveStage> getActiveCases() {
        return activeStageRepository.findAll();
    }
}
