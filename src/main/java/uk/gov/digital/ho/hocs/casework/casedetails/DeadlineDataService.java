package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.DeadlineDataDto;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class DeadlineDataService {
    private final DeadlineDataRepository deadlineDataRepository;

    @Autowired
    public DeadlineDataService(
            DeadlineDataRepository deadlineDataRepository
    ) {
        this.deadlineDataRepository = deadlineDataRepository;
    }

    @Transactional
    public DeadlineData updateDeadlines(UUID caseUUID, Set<DeadlineDataDto> deadlines){
        log.debug("updating Deadlines for Case UUID: {}", caseUUID);
        for (DeadlineDataDto deadline : deadlines) {
            DeadlineData deadlineData = deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline.getStage());
            if (deadlineData != null) {
                deadlineData.update(deadline.getDate(), deadline.getStage());
                deadlineDataRepository.save(deadlineData);
                //TODO Audit
                log.info("Updated {} Deadline for case - {}", deadline.getStage(), caseUUID);
            } else {
                DeadlineData d = new DeadlineData(caseUUID, deadline.getDate(), deadline.getStage());
                deadlineDataRepository.save(d);
                //TODO Audit
                log.info("created entry for {} Deadline for case - {}", deadline.getStage(), caseUUID);
            }
        }
        return null;
    }
}