package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Reference;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ReferenceDataRepository;

import javax.transaction.Transactional;
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
    public void createReference(UUID caseUUID, String reference, ReferenceType type) {
        log.debug("create Reference Data for Case UUID: {}", caseUUID);
        Reference referenceData = new Reference(caseUUID, type, reference);
                referenceDataRepository.save(referenceData);
                //TODO Audit
                log.info("Updated reference data for Case UUID: {}", caseUUID);
    }

    @Transactional
    public Reference getReferenceData(UUID caseUUID) {
        log.debug("Getting reference for case UUID: {}", caseUUID);
        //TODO Audit
        Reference reference = referenceDataRepository.findByCaseUUID(caseUUID);
        if (reference != null) {
            log.info("Got reference Data for Case UUID: {}", caseUUID);
            return reference;
        } else {
            throw new EntityNotFoundException("reference not found for case id: %s", caseUUID);
        }
    }

}
