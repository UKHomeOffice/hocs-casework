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
    public DeadlineDataService(DeadlineDataRepository deadlineDataRepository) {
        this.deadlineDataRepository = deadlineDataRepository;
    }

    @Transactional
    public void updateDeadlines(UUID caseUUID, Set<DeadlineDataDto> deadlines) {
        log.debug("Updating Deadlines for Case UUID: {}", caseUUID);
        for (DeadlineDataDto deadline : deadlines) {
            DeadlineData deadlineData = deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, deadline.getStage());
            if (deadlineData != null) {
                deadlineData.update(deadline.getDate(), deadline.getStage());
                deadlineDataRepository.save(deadlineData);
                //TODO Audit
                log.info("Updated {} Deadline for Case UUID: {}", deadline.getStage(), caseUUID);
            } else {
                DeadlineData d = new DeadlineData(caseUUID, deadline.getDate(), deadline.getStage());
                deadlineDataRepository.save(d);
                //TODO Audit
                log.info("Created {} Deadline for Case UUID: {}", deadline.getStage(), caseUUID);
            }
        }
    }

    public Set<DeadlineData> getDeadlinesForCase(UUID caseUUID) {
        log.debug("Getting Deadlines for Case UUID: {}", caseUUID);
        Set<DeadlineData> deadlines = deadlineDataRepository.findAllByCaseUUID(caseUUID);
        log.info("Got Deadlines for Case UUID: {}", caseUUID);
        return deadlines;
    }
}