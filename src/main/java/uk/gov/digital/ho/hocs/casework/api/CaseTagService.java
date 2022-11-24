package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataTag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseTagRepository;

import javax.transaction.Transactional;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_TAG_CONFLICT;

@Service
public class CaseTagService {

    private final CaseTagRepository caseTagRepository;

    public CaseTagService(CaseTagRepository caseTagRepository) {
        this.caseTagRepository = caseTagRepository;
    }

    public CaseDataTag addTagToCase(UUID caseUuid, String tag) {
        try {
            return caseTagRepository.save(new CaseDataTag(caseUuid, tag));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationExceptions.DatabaseConflictException("Failed to add data tag for case",
                CASE_TAG_CONFLICT, ex);
        }
    }

    @Transactional
    public void removeTagFromCase(UUID caseUuid, String tag) {
        caseTagRepository.deleteByCaseUuidAndTag(caseUuid, tag);
    }

}