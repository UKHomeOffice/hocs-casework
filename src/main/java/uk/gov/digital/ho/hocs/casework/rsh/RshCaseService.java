package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.model.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;
import uk.gov.digital.ho.hocs.casework.model.StageDetails;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class RshCaseService {


    private ObjectMapper objectMapper = new ObjectMapper();

    private final RshCaseRepository rshCaseRepository;

    @Autowired
    public RshCaseService(RshCaseRepository rshCaseRepository) {

        this.rshCaseRepository = rshCaseRepository;
    }

    CaseDetails createRSHCase(String caseType, StageDetails stageDetails) {
        // This stuff should be set by the client
        UUID requestUUID = UUID.randomUUID();
        LocalDateTime requestTimestamp = LocalDateTime.now();
        String stageName = "onlyStage";
        String caseType = "RSH";
        CaseSaveRequest caseSaveRequest = CaseSaveRequest.from(requestUUID, requestTimestamp, caseType, stageName, request);

        CaseDetails rshCaseDetails = new CaseDetails(caseType,rshCaseRepository.getNextSeriesId(), stageDetails);
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    CaseDetails createCase(String caseType, CaseDetails caseDetails) {
        CaseDetails rshCaseDetails = new CaseDetails(caseType,rshCaseRepository.getNextSeriesId(), stageDetails);
        rshCaseRepository.save(caseDetails);
        return rshCaseDetails;
    }

    CaseDetails updateRSHCase(String uuid, Map<String,String> caseData) {
        CaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(objectMapper.writeValueAsString(caseData));
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    CaseDetails getRSHCase(String uuid) {
        CaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

    List<CaseDetails> findCases(SearchRequest searchRequest){

        ArrayList<CaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            CaseDetails result = rshCaseRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.add(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null)
        {
            Map<String, String> searchdata = searchRequest.getCaseData();
            Set<CaseDetails> resultList = rshCaseRepository.findByNameOrDob(searchdata.get("first-name"),searchdata.get("last-name"), searchdata.get("date-of-birth"));
            results.addAll(resultList);
        }
        return results;
    }

}

