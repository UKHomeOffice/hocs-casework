package uk.gov.digital.ho.hocs.casework.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetailsRepository;

import javax.transaction.Transactional;
import java.util.*;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
class SearchService {

    private final AuditService auditService;
    private final CaseDetailsRepository caseDetailsRepository;

    @Autowired
    public SearchService(AuditService auditService, CaseDetailsRepository caseDetailsRepository) {
        this.auditService = auditService;
        this.caseDetailsRepository = caseDetailsRepository;
    }

    private static String getFieldString(Map<String, String> stageData, String key) {
        String ret = "";
        if (stageData != null && stageData.containsKey(key)) {
            String val = stageData.get(key);
            if (val != null) {
                ret = val;
            }
        }
        return ret;
    }

    @Transactional
    public List<CaseDetails> findCases(SearchRequest searchRequest, String username) {
        auditService.writeSearchEvent(username, searchRequest);
        log.info("SEARCH: Requesting Search, User: {}", username);

        List<CaseDetails> results = new ArrayList<>(findByCaseReference(searchRequest.getCaseReference()));

        if (results.isEmpty()) {
            results.addAll(findByNameOrDob(searchRequest.getCaseData()));
        }

        log.info("SEARCH: Returned Search, Found: {}, User: {}", results.size(), username);
        return results;
    }

    private Set<CaseDetails> findByCaseReference(String caseReference) {
        Set<CaseDetails> returnResults = new HashSet<>();

        if (!isNullOrEmpty(caseReference)) {
            Set<CaseDetails> results = caseDetailsRepository.findByCaseReference(caseReference);
            if (results != null && results.isEmpty()) {
                returnResults.addAll(results);
            }
        }
        return returnResults;
    }

    private Set<CaseDetails> findByNameOrDob(Map<String, String> searchMap) {
        Set<CaseDetails> returnResults = new HashSet<>();

        if (searchMap != null && !searchMap.isEmpty()) {
            Set<CaseDetails> results = caseDetailsRepository.findByNameOrDob(getFieldString(searchMap, "first-name"), getFieldString(searchMap, "last-name"), getFieldString(searchMap, "date-of-birth"));
            if (results != null && !results.isEmpty()) {
                returnResults.addAll(results);
            }
        }
        return returnResults;
    }
}