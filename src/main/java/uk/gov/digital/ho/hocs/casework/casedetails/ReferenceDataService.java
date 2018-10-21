package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Reference;
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
    public void createReference(UUID caseUUID, String reference, ReferenceType type) {
        Reference referenceData = new Reference(caseUUID, type, reference);
        referenceDataRepository.save(referenceData);
        log.info("Updated reference data for Case: {}", caseUUID);
    }

    @Transactional
    public Set<Reference> getReference(UUID caseUUID) {
        Set<Reference> reference = referenceDataRepository.findByCaseUUID(caseUUID);
        if (reference != null) {
            log.info("Got Reference for Case: {}", caseUUID);
            return reference;
        } else {
            throw new EntityNotFoundException("reference not found for case id: %s", caseUUID);
        }
    }

}
