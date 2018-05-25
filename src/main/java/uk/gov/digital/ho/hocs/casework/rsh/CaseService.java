package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;

import java.util.*;

@Service
@Slf4j
public class CaseService {


    private ObjectMapper objectMapper = new ObjectMapper();

    private final CaseDetailsRepository caseDetailsRepository;
    private final StageDetailsRepository stageDetailsRepository;

    @Autowired
    public CaseService(CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository) {

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
    }

    CaseDetails createCase(String caseType) {
        CaseDetails caseDetails = new CaseDetails(caseType, caseDetailsRepository.getNextSeriesId());
        caseDetailsRepository.save(caseDetails);
        return caseDetails;
    }

    void createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String,Object> stageData) {

        String data = getDataString(stageData);
        StageDetails stageDetails = new StageDetails(caseUUID, stageName, schemaVersion, data);
        stageDetailsRepository.save(stageDetails);
    }

    void updateStage(UUID stageUUID, int schemaVersion, Map<String,Object> stageData) {

        StageDetails stageDetails = stageDetailsRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData);
            stageDetails.setData(data);
            stageDetailsRepository.save(stageDetails);
        }
    }

    CaseDetails getRSHCase(UUID uuid) {
        CaseDetails rshCaseDetails = caseDetailsRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

    List<CaseDetails> findCases(SearchRequest searchRequest){

        ArrayList<CaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            CaseDetails result = caseDetailsRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.add(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null)
        {
            Map<String, Object> searchdata = searchRequest.getCaseData();
            //Set<CaseDetails> resultList = caseDetailsRepository.findByNameOrDob(searchdata.get("first-name"),searchdata.get("last-name"), searchdata.get("date-of-birth"));
           // results.addAll(resultList);
        }
        return results;
    }

    private String getDataString(Map<String, Object> stageData) {
        String data = null;
        try {
            data = objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }

}

