package uk.gov.digital.ho.hocs.casework.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import javax.transaction.Transactional;
import java.util.*;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
class SearchService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;

    @Autowired
    public SearchService(AuditService auditService, CaseDataRepository caseDataRepository) {
        this.auditService = auditService;
        this.caseDataRepository = caseDataRepository;
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
    public List<CaseData> findCases(SearchRequest searchRequest, String username) {
        auditService.writeSearchEvent(username, searchRequest);
        log.info("SEARCH: Requesting Search, User: {}", username);

        List<CaseData> results = new ArrayList<>(findByCaseReference(searchRequest.getCaseReference()));

        if (results.isEmpty()) {
            results.addAll(findByNameOrDob(searchRequest.getCaseData()));
        }

        log.info("SEARCH: Returned Search, Found: {}, User: {}", results.size(), username);
        return results;
    }

    private Set<CaseData> findByCaseReference(String caseReference) {
        Set<CaseData> returnResults = new HashSet<>();

        if (!isNullOrEmpty(caseReference)) {
            Set<CaseData> results = caseDataRepository.findByCaseReference(caseReference);
            if (results != null && results.isEmpty()) {
                returnResults.addAll(results);
            }
        }
        return returnResults;
    }

    private Set<CaseData> findByNameOrDob(Map<String, String> searchMap) {
        Set<CaseData> returnResults = new HashSet<>();

        if (searchMap != null && !searchMap.isEmpty()) {
            Set<CaseData> results = caseDataRepository.findByNameOrDob(getFieldString(searchMap, "first-name"), getFieldString(searchMap, "last-name"), getFieldString(searchMap, "date-of-birth"));
            if (results != null && !results.isEmpty()) {
                returnResults.addAll(results);
            }
        }
        return returnResults;
    }
}