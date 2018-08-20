package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDeadlineRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDeadlinesRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DeadlineDataRepository;

import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final InputDataService inputDataService;
    private final DeadlineDataRepository deadlineDataRepository;


    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           InputDataService inputDataService,
                           AuditService auditService,
                           DeadlineDataRepository deadlineDataRepository
    ) {
        this.caseDataRepository = caseDataRepository;
        this.inputDataService = inputDataService;
        this.deadlineDataRepository = deadlineDataRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CaseData createCase(CaseType caseType) {
        log.debug("Creating Case, Type: {}", caseType);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        InputData inputData = inputDataService.createInputData(caseData.getUuid());
        caseData.setInputData(inputData);
        auditService.createCaseEvent(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType, caseData.getUuid());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case UUID: {}", caseUUID);
        CaseData caseData = getCaseData(caseUUID);
        InputData inputData = inputDataService.getInputData(caseData.getUuid());
        caseData.setInputData(inputData);
        auditService.getCaseEvent(caseUUID);
        log.info("Got Case UUID: {}", caseData.getUuid());
        return caseData;
    }

    private CaseData getCaseData(UUID caseUUID) {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            return caseData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
        }
    }

    @Transactional
    public DeadlineData updateDeadlines(UUID caseUUID, Set<UpdateDeadlineRequest> deadlines){
        log.debug("updating Deadlines for Case UUID: {}", caseUUID);
        for (UpdateDeadlineRequest deadline : deadlines) {
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