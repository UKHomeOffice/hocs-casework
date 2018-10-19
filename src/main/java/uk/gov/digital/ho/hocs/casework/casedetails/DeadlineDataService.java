package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageDeadline;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
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
        Set<StageDeadline> stageDeadlines = new HashSet<>();
        for (Map.Entry<StageType, LocalDate> deadline : deadlines.entrySet()) {
            StageDeadline stageDeadline = new StageDeadline(caseUUID, deadline.getKey(), deadline.getValue());
            stageDeadlines.add(stageDeadline);
        }
        deadlineDataRepository.saveAll(stageDeadlines);
        log.info("Created {} Deadlines for Case: {}", stageDeadlines.size(), caseUUID);
    }

}