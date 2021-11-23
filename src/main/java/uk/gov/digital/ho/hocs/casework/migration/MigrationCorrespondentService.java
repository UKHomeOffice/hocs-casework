package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class MigrationCorrespondentService {

    private final CorrespondentRepository correspondentRepository;

    @Autowired
    public MigrationCorrespondentService(CorrespondentRepository correspondentRepository) {
        this.correspondentRepository = correspondentRepository;
    }

    UUID createCorrespondent(UUID caseUUID, String correspondentType, String fullname, String organisation, Address address, String telephone, String email, String reference, String externalKey) {
        log.debug("Creating Correspondent of Type: {} for Case: {}", correspondentType, caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID, correspondentType, fullname, organisation, address, telephone, email, reference, externalKey);
        try {
            correspondentRepository.save(correspondent);

        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Failed to create correspondent %s for Case: %s", correspondent.getUuid(), caseUUID), CORRESPONDENT_CREATE_FAILURE);
        }
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID, value(EVENT, CORRESPONDENT_CREATED));
        return correspondent.getUuid();
    }
}