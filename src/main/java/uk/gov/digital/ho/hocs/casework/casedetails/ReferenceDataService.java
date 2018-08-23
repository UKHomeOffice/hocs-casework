package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ReferenceDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ReferenceDataService {

    private final ReferenceDataRepository referenceDataRepository;

    @Autowired
    public ReferenceDataService(ReferenceDataRepository referenceDataRepository) {
        this.referenceDataRepository = referenceDataRepository;
    }

    @Transactional
    public void createReference(UUID caseUUID, ReferenceType type, String reference) {
        log.debug("create Reference Data for Case UUID: {}", caseUUID);
        ReferenceData referenceData = new ReferenceData(caseUUID,type,reference);
                referenceDataRepository.save(referenceData);
                //TODO Audit
                log.info("Updated reference data for Case UUID: {}", caseUUID);
    }

    @Transactional
    public ReferenceData getReferenceData(UUID caseUUID) {
        log.debug("Getting referenceData for case UUID: {}", caseUUID);
        //TODO Audit
        ReferenceData referenceData = referenceDataRepository.findByCaseUUID(caseUUID);
        if (referenceData != null) {
            log.info("Got referenceData Data for Case UUID: {}", caseUUID);
            return referenceData;
        } else {
            throw new EntityNotFoundException("referenceData not found for case id: %s", caseUUID);
        }
    }

}
