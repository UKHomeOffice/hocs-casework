package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RshCaseService {

    private final String apiKey;
    private final String rshTemplateId;
    private final String url;

    NotificationClient client;
    ObjectMapper objectMapper = new ObjectMapper();

    private final RshCaseRepository rshCaseRepository;

    @Autowired
    public RshCaseService(@Value("${notify.apiKey}") String apiKey,
                          @Value("${notify.rshTemplateId}") String rshTemplateId,
                          @Value("${env.url}") String url,
                          RshCaseRepository rshCaseRepository) {
        this.apiKey = apiKey;
        this.rshTemplateId = rshTemplateId;
        this.url = url;
        this.rshCaseRepository = rshCaseRepository;
        client = new NotificationClient(apiKey);
    }

    RshCaseDetails createRSHCase(Map<String,String> caseData) throws JsonProcessingException {

        RshCaseDetails rshCaseDetails = new RshCaseDetails("RSH",rshCaseRepository.getNextSeriesId(), objectMapper.writeValueAsString(caseData));
        rshCaseRepository.save(rshCaseDetails);
        if (caseSaveRequest.getNotifyEmail() != null && !caseSaveRequest.getNotifyEmail().isEmpty()) {
            sendEmail(rshCaseDetails.getUuid(), caseSaveRequest.getNotifyEmail(), caseSaveRequest.getNotifyTeamName());
        }
        return rshCaseDetails;
    }

    RshCaseDetails updateRSHCase(String uuid, Map<String,String> caseData) throws JsonProcessingException {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(objectMapper.writeValueAsString(caseData));
        rshCaseRepository.save(rshCaseDetails);
        if (caseSaveRequest.getNotifyEmail() != null && !caseSaveRequest.getNotifyEmail().isEmpty()) {
            sendEmail(rshCaseDetails.getUuid(), caseSaveRequest.getNotifyEmail(), caseSaveRequest.getNotifyTeamName());
        }
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

    private void sendEmail(String uuid, String emailAddress, String teamName) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", url + "/rsh/case/" + uuid);
        try {
            client.sendEmail(rshTemplateId, emailAddress, personalisation, null, null);
        } catch (NotificationClientException e) {
            e.printStackTrace();
        }
    }

}

