package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CorrespondentDto;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseCorrespondent;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
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

    @Transactional
    public void addCorrespondentToCase(UUID caseUUID, CorrespondentDto correspondent) {
        log.debug("adding Correspondent to case UUID: {}", caseUUID);
        //save correspondent
        CorrespondentData correspondentData =
                new CorrespondentData(
                        correspondent.getTitle(),
                        correspondent.getFirstName(),
                        correspondent.getSurname(),
                        correspondent.getPostcode(),
                        correspondent.getAddress1(),
                        correspondent.getAddress2(),
                        correspondent.getAddress3(),
                        correspondent.getCountry(),
                        correspondent.getTelephone(),
                        correspondent.getEmail()
                );
        correspondentDataRepository.save(correspondentData);
        //save case to correspondent link with type
        caseCorrespondentRepository.save(new CaseCorrespondent(caseUUID, correspondentData.getUuid(), correspondent.getType()))
        //TODO Audit
        ;
    }

    @Transactional
    public Set<CorrespondentData> getCorrespondents(UUID caseUUID) {
        log.debug("Getting all Correspondents for case UUID: {}", caseUUID);
        //TODO Audit
        Set<CorrespondentData> correspondents = correspondentDataRepository.findByCaseUUID(caseUUID);
        if (correspondents != null) {
            log.info("Got correspondent Data for Case UUID: {}", caseUUID);
            return correspondents;
        } else {
            throw new EntityNotFoundException("Correspondents not found for case id: %s", caseUUID);
        }
    }
}
