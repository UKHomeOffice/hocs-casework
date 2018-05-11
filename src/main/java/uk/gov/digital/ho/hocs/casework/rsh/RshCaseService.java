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
import java.util.stream.Collectors;


@Service
@Slf4j
public class RshCaseService {

    ObjectMapper objectMapper = new ObjectMapper();

    private final RshCaseRepository rshCaseRepository;

    @Autowired
    public RshCaseService(RshCaseRepository rshCaseRepository) {
        this.rshCaseRepository = rshCaseRepository;
    }

    RshCaseDetails createRSHCase(String caseData) {

        RshCaseDetails rshCaseDetails = new RshCaseDetails("RSH",rshCaseRepository.getNextSeriesId(), caseData);
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    RshCaseDetails updateRSHCase(String uuid, String data) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(data);
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails;
    }

    RshCaseDetails getRSHCase(String uuid) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

    List<RshCaseDetails> findCases(SearchRequest searchRequest){

        ArrayList<RshCaseDetails> rshCaseDetailsList = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            RshCaseDetails result = rshCaseRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                rshCaseDetailsList.add(result);
            }
        }
        if (searchRequest.getCaseData() != null)
        {
            Map<String, String> searchdata =  searchRequest.getCaseData().entrySet().stream().collect(Collectors.toMap(es -> es.getKey(), es-> toJson(es)));
            Set<RshCaseDetails> results = rshCaseRepository.findByNameOrDob(searchdata.get("firstname"),searchdata.get("lastname"), searchdata.get("dob"));
            rshCaseDetailsList.addAll(results);
        }
        return rshCaseDetailsList;
    }

    private String toJson(Map.Entry<String,String> entry){

        String value = "{}";
        try {
            value = objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return value;
    }
}

