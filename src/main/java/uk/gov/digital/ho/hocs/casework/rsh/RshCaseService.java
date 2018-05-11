package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class RshCaseService {

    ObjectMapper objectMapper = new ObjectMapper();

    private final RshCaseRepository rshCaseRepository;

    @Autowired
    public RshCaseService(RshCaseRepository rshCaseRepository) {
        this.rshCaseRepository = rshCaseRepository;
    }

    RshCaseDetails createRSHCase(Map<String,String> caseData) throws JsonProcessingException {

        RshCaseDetails rshCaseDetails = new RshCaseDetails("RSH",rshCaseRepository.getNextSeriesId(), objectMapper.writeValueAsString(caseData));
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    RshCaseDetails updateRSHCase(String uuid, Map<String,String> caseData) throws JsonProcessingException {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(objectMapper.writeValueAsString(caseData));
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    RshCaseDetails getRSHCase(String uuid) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

    List<RshCaseDetails> findCases(SearchRequest searchRequest){

        ArrayList<RshCaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            RshCaseDetails result = rshCaseRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.add(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null)
        {
            Map<String, String> searchdata = searchRequest.getCaseData();
            Set<RshCaseDetails> resultList = rshCaseRepository.findByNameOrDob(searchdata.get("first-name"),searchdata.get("last-name"), searchdata.get("date-of-birth"));
            results.addAll(resultList);
        }
        return results;
    }
}

