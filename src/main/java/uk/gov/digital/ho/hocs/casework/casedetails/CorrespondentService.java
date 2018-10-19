package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CorrespondentDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CorrespondentService {

    private final CorrespondentDataRepository correspondentDataRepository;

    @Autowired
    public CorrespondentService(CorrespondentDataRepository correspondentDataRepository) {
        this.correspondentDataRepository = correspondentDataRepository;
    }

    public void createCorrespondent(UUID caseUUID, String fullname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, CorrespondentType correspondentType) {
        Correspondent correspondent = new Correspondent(fullname,
                postcode,
                address1,
                address2,
                address3,
                country,
                telephone,
                email);

        correspondentDataRepository.save(correspondent);
        log.debug("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID);
    }

    @Transactional
    public Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        Correspondent correspondent = correspondentDataRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent != null) {
            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
            return correspondent;
        } else {
            throw new EntityNotFoundException("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID);
        }
    }

    @Transactional
    public Set<Correspondent> getCorrespondents(UUID caseUUID) {
        Set<Correspondent> correspondents = correspondentDataRepository.findByCaseUUID(caseUUID);
        log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID);
        return correspondents;
    }

    @Transactional
    public void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        correspondentDataRepository.delete(correspondentUUID);
        log.info("Deleted Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
    }
}
