package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import javax.transaction.Transactional;
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

    @Transactional
    public Set<Correspondent> getCorrespondents(UUID caseUUID) {
        Set<Correspondent> correspondents = correspondentRepository.findAllByCaseUUID(caseUUID);
        if (correspondents != null) {
            log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
            return correspondents;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Correspondents for Case UUID: %s not found!", caseUUID),  CORRESPONDENT_NOT_FOUND);
        }
    }

    @Transactional
    public Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        Correspondent correspondent = correspondentRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent != null) {
            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_RETRIEVED));
            return correspondent;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID), CORRESPONDENT_NOT_FOUND);
        }
    }

    @Transactional
    public void createCorrespondent(UUID caseUUID, String correspondentType, String fullname, Address address, String telephone, String email, String reference) {
        Correspondent correspondent = new Correspondent(caseUUID,
                correspondentType,
                fullname,
                address,
                telephone,
                email,
                reference);

        correspondentRepository.save(correspondent);
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID, value(EVENT, CORRESPONDENT_CREATED));
    }

    @Transactional
    public void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        correspondentRepository.deleteCorrespondent(correspondentUUID);
        log.info("Deleted Correspondent: {} for Case: {}", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_DELETED));
    }
}