package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ActiveStageRepository;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ActiveStageService {

    private final AuditService auditService;
    private final ActiveStageRepository activeStageRepository;



    @Autowired
    public ActiveStageService(ActiveStageRepository activeStageRepository,
                              AuditService auditService) {
        this.auditService = auditService;
        this.activeStageRepository = activeStageRepository;
    }

    //TODO: This method is a dev tool
    public Set<ActiveStage> getActiveCases() {
        return activeStageRepository.findAllActiveStages();
    }

    public Set<ActiveStage> getActiveStagesByUserUUID(UUID userUUID) {
        return activeStageRepository.findAllActiveStages();
    }

    public Set<ActiveStage> getActiveStagesByTeamUUID(Set<UUID> teamUUIDs) {
        log.debug("GETTING ALL ACTIVE STAGES.");
        return activeStageRepository.findAllActiveStages();
    }

}