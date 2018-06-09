package uk.gov.digital.ho.hocs.casework.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.AuditRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetailsRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
class SearchService {

    private final AuditRepository auditRepository;
    private final CaseDetailsRepository caseDetailsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SearchService(CaseDetailsRepository caseDetailsRepository, AuditRepository auditRepository) {

        this.caseDetailsRepository = caseDetailsRepository;
        this.auditRepository = auditRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    @Transactional
   public List<CaseDetails> findCases(SearchRequest searchRequest, String username){
        String request = SearchRequest.toJsonString(objectMapper, searchRequest);
        log.info("Requesting Search, User: {}", username);
        ArrayList<CaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null){
            Set<CaseDetails> result = caseDetailsRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.addAll(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null){
            Map<String, Object> searchData = searchRequest.getCaseData();
            Set<CaseDetails> resultList = caseDetailsRepository.findByNameOrDob(getFieldString(searchData,"first-name"), getFieldString(searchData,"last-name"), getFieldString(searchData,"date-of-birth"));
            results.addAll(resultList);
        }

        AuditEntry auditEntry = new AuditEntry(username, request, AuditAction.SEARCH);
        auditRepository.save(auditEntry);
        log.info("Returned Search, Found: {}, User: {}", results.size(), username);
        return results;
    }

    private static String getFieldString(Map<String, Object> stageData, String key) {
        String ret = "";
        if(stageData.containsKey(key)){
            String val = stageData.get(key).toString();
            if(val != null) {
                ret = val;
            }
        }
        return ret;
    }
}

