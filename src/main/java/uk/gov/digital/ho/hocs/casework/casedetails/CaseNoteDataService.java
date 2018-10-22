package uk.gov.digital.ho.hocs.casework.casedetails;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseNoteDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CaseNoteDataService {

    private final CaseNoteDataRepository caseNoteDataRepository;

    @Autowired
    public CaseNoteDataService(CaseNoteDataRepository caseNoteDataRepository) {
        this.caseNoteDataRepository = caseNoteDataRepository;
    }

    @Transactional
    public CaseNoteData createCaseNote(UUID caseUUID, String caseNote) {
        log.debug("Creating CaseNote, case: {}", caseUUID);
        CaseNoteData caseNoteData = new CaseNoteData(caseUUID, caseNote);
        caseNoteDataRepository.save(caseNoteData);
        log.info("Created CaseNote case: {}", caseUUID);
        return caseNoteData;
    }

    public Set<CaseNoteData> getCaseNote(UUID caseUUID) {
        log.debug("Getting Case Note UUID: {}", caseUUID);
        Set<CaseNoteData> caseNoteData = caseNoteDataRepository.findAllByCaseUUID(caseUUID);
        if (caseNoteData != null && caseNoteData.size() != 0) {
            log.info("Got Case UUID: {}", caseUUID);
            return caseNoteData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
        }
    }
}
