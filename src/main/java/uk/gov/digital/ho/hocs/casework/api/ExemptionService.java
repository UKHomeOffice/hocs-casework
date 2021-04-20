package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.ExemptionRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class ExemptionService {

    private final ExemptionRepository exemptionRepository;
    private final AuditClient auditClient;

    @Autowired
    public ExemptionService(
            ExemptionRepository exemptionRepository,
            AuditClient auditClient) {
        this.exemptionRepository = exemptionRepository;
        this.auditClient = auditClient;
    }

    Set<Exemption> getExemptions(UUID caseUUID) {
        log.debug("Getting all Exemptions for Case: {}", caseUUID);
        Set<Exemption> exemptions = exemptionRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} Exemptions for Case: {}", exemptions.size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return exemptions;
    }

    void createExemption(UUID caseUUID, String exemptionType){
        log.debug("Creating Exemption of Type: {} for Case: {}", exemptionType, caseUUID);
        Exemption exemption = new Exemption(caseUUID, exemptionType);
        try {
            exemptionRepository.save(exemption);


        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Failed to create exemption %s for Case: %s", exemption.getUuid(), caseUUID), CORRESPONDENT_CREATE_FAILURE, e);
        }
    }
}
