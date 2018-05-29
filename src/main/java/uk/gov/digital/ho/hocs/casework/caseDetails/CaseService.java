package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CaseService {

    private final CaseDetailsRepository caseDetailsRepository;
    private final StageDetailsRepository stageDetailsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseService(CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, ObjectMapper objectMapper) {

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
        this.objectMapper = objectMapper;
    }

    CaseDetails createRshCase(Map<String, Object> caseData) {
        CaseDetails caseDetails = createCase("RSH");
        createStage(caseDetails.getUuid(),"OnlyStage", 0, caseData);
        return caseDetails;
    }

    CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData) {
        CaseDetails caseDetails = getRSHCase(caseUUID);
        if(!caseDetails.getStages().isEmpty()) {
            StageDetails stageDetails = caseDetails.getStages().iterator().next();
            updateStage(stageDetails.getUuid(),0,caseData);
        }
        return caseDetails;
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

    StageDetails updateStage(UUID stageUUID, int schemaVersion, Map<String,Object> stageData) {

        StageDetails stageDetails = stageDetailsRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData);
            stageDetails.setData(data);
            stageDetails.setUpdated(LocalDateTime.now());
            stageDetailsRepository.save(stageDetails);
        }
        return stageDetails;
    }

    CaseDetails getRSHCase(UUID uuid) {
        CaseDetails rshCaseDetails = caseDetailsRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

    List<CaseDetails> findCases(SearchRequest searchRequest){

        ArrayList<CaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            Set<CaseDetails> result = caseDetailsRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.addAll(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null)
        {
            Map<String, Object> searchdata = searchRequest.getCaseData();
            Set<CaseDetails> resultList = caseDetailsRepository.findByNameOrDob(getFieldString(searchdata,"first-name"), getFieldString(searchdata,"last-name"), getFieldString(searchdata,"date-of-birth"));
            results.addAll(resultList);
        }
        return results;
    }

    private String getFieldString(Map<String, Object> stageData, String key) {
        String ret = "";
        if(stageData.containsKey(key)){
            String val = stageData.get(key).toString();
            if(val != null) {
                ret = val;
            }
        }
        return ret;

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

