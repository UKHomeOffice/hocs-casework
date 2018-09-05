package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
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
    public void updateDeadlines(UUID caseUUID, Map<StageType, LocalDate> deadlines) {
        log.debug("Updating Deadlines for Case UUID: {}", caseUUID);
        for (Map.Entry<StageType, LocalDate> deadline : deadlines.entrySet()) {
            String stageTypeString = deadline.getKey().toString();
            DeadlineData deadlineData = deadlineDataRepository.findByCaseUUIDAndStage(caseUUID, stageTypeString);
            if (deadlineData != null) {
                deadlineData.update(deadline.getValue(), deadline.getKey());
                deadlineDataRepository.save(deadlineData);
                //TODO Audit
                log.info("Updated {} Deadline for Case UUID: {}", deadline.getKey(), caseUUID);
            } else {
                DeadlineData d = new DeadlineData(caseUUID, deadline.getKey(), deadline.getValue());
                deadlineDataRepository.save(d);
                //TODO Audit
                log.info("Created {} Deadline for Case UUID: {}", deadline.getKey(), caseUUID);
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