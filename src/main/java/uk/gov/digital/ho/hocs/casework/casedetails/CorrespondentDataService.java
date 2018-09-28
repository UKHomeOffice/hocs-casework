package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseCorrespondent;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseCorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CorrespondentDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CorrespondentDataService {

    private final CorrespondentDataRepository correspondentDataRepository;
    private final CaseCorrespondentRepository caseCorrespondentRepository;

    @Autowired
    public CorrespondentDataService(CorrespondentDataRepository correspondentDataRepository, CaseCorrespondentRepository caseCorrespondentRepository) {
        this.correspondentDataRepository = correspondentDataRepository;
        this.caseCorrespondentRepository = caseCorrespondentRepository;
    }

    public void createCorrespondent(UUID caseUUID, String fullname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, CorrespondentType correspondentType) {

        if (correspondentType != null) {
            log.debug("Not found Correspondent, creating a new one");
            CorrespondentData correspondentData = new CorrespondentData(fullname,
                    postcode,
                    address1,
                    address2,
                    address3,
                    country,
                    telephone,
                    email);

            correspondentDataRepository.save(correspondentData);

            assignCorrespondentToCase(caseUUID, correspondentData.getUuid(), correspondentType);
        } else {
            throw new EntityCreationException("CorrespondentType is null");
        }
    }

    @Transactional
    public CorrespondentData getCorrespondent(UUID correspondentUUID) {
        log.debug("Getting all correspondent: {}", correspondentUUID);
        CorrespondentData correspondent = correspondentDataRepository.findByUUID(correspondentUUID);
        if (correspondent != null) {
            log.info("Got correspondent data: {}", correspondentUUID);
            return correspondent;
        } else {
            throw new EntityNotFoundException("Correspondents not found for case id: %s", correspondentUUID);
        }
    }

    @Transactional
    public Set<CorrespondentData> getCorrespondents(UUID caseUUID) {
        log.debug("Getting all Correspondents for case UUID: {}", caseUUID);
        Set<CorrespondentData> correspondents = correspondentDataRepository.findByCaseUUID(caseUUID);
        if (correspondents != null) {
            log.info("Got correspondent Data for Case UUID: {}", caseUUID);
            return correspondents;
        } else {
            throw new EntityNotFoundException("Correspondents not found for case id: %s", caseUUID);
        }
    }

    public void assignCorrespondentToCase(UUID caseUUID, UUID correspondentUUID, CorrespondentType type) {
        log.debug("Finding or creating a Case Correspondent link");
        CaseCorrespondent caseCorrespondent = caseCorrespondentRepository.findByCaseUUIDAndCorrespondentUUID(caseUUID, correspondentUUID);

        if (caseCorrespondent != null) {
            log.debug("Found link, updating");
            caseCorrespondent.update(type);
        } else {
            log.debug("Not found link, creating a new one");
            caseCorrespondent = new CaseCorrespondent(caseUUID, correspondentUUID, type);
        }
        caseCorrespondentRepository.save(caseCorrespondent);
    }

    public void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        log.debug("deleting a Case Correspondent link");
        CaseCorrespondent caseCorrespondent = caseCorrespondentRepository.findByCaseUUIDAndCorrespondentUUID(caseUUID, correspondentUUID);
        if (caseCorrespondent != null) {
            log.debug("Found link, soft deleting");
            caseCorrespondent.delete();
        } else {
            throw new EntityNotFoundException("Correspondent not found for case id: %s", caseUUID);
        }
        caseCorrespondentRepository.save(caseCorrespondent);
    }
}
