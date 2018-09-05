package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public void findOrCreateCorrespondent(UUID caseUUID, String title, String firstname, String surname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, CorrespondentType correspondentType, String addressIdentity, String emailIdentity, String telephoneIdentity) {
        log.debug("Finding or creating a Correspondent");

        CorrespondentData correspondentData = null;
        //get case by identity
        if (addressIdentity != null) {
            log.debug("Checking AddressIdentity");
            correspondentData = correspondentDataRepository.findByAddressIdentity(addressIdentity);
        }
        if (correspondentData == null && emailIdentity != null) {
            log.debug("Checking EmailIdentity");
            correspondentData = correspondentDataRepository.findByEmailIdentity(emailIdentity);
        }
        if (correspondentData == null && telephoneIdentity != null) {
            log.debug("Checking TelephoneIdentity");
            correspondentData = correspondentDataRepository.findByTelephoneIdentity(telephoneIdentity);
        }

        if (correspondentData != null) {
            log.debug("Found Correspondent");
            correspondentData.update(
                    title,
                    firstname,
                    surname,
                    postcode,
                    address1,
                    address2,
                    address3,
                    country,
                    telephone,
                    email,
                    addressIdentity,
                    emailIdentity,
                    telephoneIdentity);

        } else {
            log.debug("Not found Correspondent, creating a new one");
            correspondentData =
                new CorrespondentData(
                        title,
                        firstname,
                        surname,
                        postcode,
                        address1,
                        address2,
                        address3,
                        country,
                        telephone,
                        email,
                        addressIdentity,
                        emailIdentity,
                        telephoneIdentity);
        }
        correspondentDataRepository.save(correspondentData);

        assignCorrespondentToCase(caseUUID, correspondentData.getUuid(), correspondentType);
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
}
