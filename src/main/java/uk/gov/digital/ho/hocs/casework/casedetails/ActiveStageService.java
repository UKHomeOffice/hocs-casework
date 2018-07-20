package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
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
    public void addActiveStage(UUID stageUUID, String stageType, UUID caseUUID, String caseReference, String caseType) {
        ActiveStage activeStage = new ActiveStage(stageUUID, stageType, caseUUID, caseReference, caseType);
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
