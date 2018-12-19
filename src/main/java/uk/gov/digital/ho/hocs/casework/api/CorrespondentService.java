package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class CorrespondentService {

    private final CorrespondentRepository correspondentRepository;

    @Autowired
    public CorrespondentService(CorrespondentRepository correspondentRepository) {
        this.correspondentRepository = correspondentRepository;
    }

    Set<Correspondent> getCorrespondents(UUID caseUUID) {
        log.debug("Getting all Correspondents for Case: {}", caseUUID);
        Set<Correspondent> correspondents = correspondentRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondents;
    }

    Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        log.debug("Getting Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
        Correspondent correspondent = correspondentRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent != null) {
            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_RETRIEVED));
            return correspondent;
        } else {
            log.error("Correspondent: {} for Case UUID: {} not found!", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID), CORRESPONDENT_NOT_FOUND);
        }
    }

    void createCorrespondent(UUID caseUUID, String correspondentType, String fullname, Address address, String telephone, String email, String reference) {
        log.debug("Creating Correspondent of Type: {} for Case: {}", correspondentType, caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID, correspondentType, fullname, address, telephone, email, reference);
        correspondentRepository.save(correspondent);
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID, value(EVENT, CORRESPONDENT_CREATED));
    }

    void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        log.debug("Deleting Correspondent: {}", correspondentUUID);
        Correspondent correspondent = getCorrespondent(caseUUID, correspondentUUID);
        correspondent.setDeleted(true);
        correspondentRepository.save(correspondent);
        log.info("Deleted Topic: {}", caseUUID, value(EVENT, CORRESPONDENT_DELETED));
    }
}