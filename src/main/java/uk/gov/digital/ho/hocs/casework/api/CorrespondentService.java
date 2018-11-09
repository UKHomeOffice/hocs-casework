package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

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
            log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID);
            return correspondents;
        } else {
            throw new EntityNotFoundException("Correspondents for Case UUID: %s not found!", caseUUID);
        }
    }

    @Transactional
    public Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        Correspondent correspondent = correspondentRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent != null) {
            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
            return correspondent;
        } else {
            throw new EntityNotFoundException("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID);
        }
    }

    @Transactional
    public Correspondent getPrimaryCorrespondent(UUID caseUUID) {
        Correspondent correspondent = correspondentRepository.getPrimaryCorrespondent(caseUUID);
        if (correspondent != null) {
            log.info("Got Primary Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID);
            return correspondent;
        } else {
            throw new EntityNotFoundException("Primary Correspondent not found for Case: %s", caseUUID);
        }
    }

    @Transactional
    public void createCorrespondent(UUID caseUUID, CorrespondentType correspondentType, String fullname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, String reference) {
        Address address = new Address(postcode,
                address1,
                address2,
                address3,
                country);

        Correspondent correspondent = new Correspondent(caseUUID,
                correspondentType,
                fullname,
                address,
                telephone,
                email,
                reference);

        correspondentRepository.save(correspondent);
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID);
    }

    @Transactional
    public void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        correspondentRepository.deleteCorrespondent(correspondentUUID);
        log.info("Deleted Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
    }
}