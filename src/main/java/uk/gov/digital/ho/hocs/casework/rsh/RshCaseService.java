package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.dto.SearchResponse;
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

    RshCaseDetails createRSHCase(CaseSaveRequest caseSaveRequest) {

        RshCaseDetails rshCaseDetails = new RshCaseDetails("RSH", rshCaseRepository.getNextSeriesId(), caseSaveRequest.getCaseData());
        rshCaseRepository.save(rshCaseDetails);
        if (caseSaveRequest.getNotifyEmail() != null && !caseSaveRequest.getNotifyEmail().isEmpty()) {
            sendEmail(rshCaseDetails.getUuid(), caseSaveRequest.getNotifyEmail(), caseSaveRequest.getNotifyTeamName());
        }
        return rshCaseDetails;
    }

    RshCaseDetails updateRSHCase(String uuid, CaseSaveRequest caseSaveRequest) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(caseSaveRequest.getCaseData());
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

    List<SearchResponse> findCases(SearchRequest searchRequest){

        ArrayList<SearchResponse> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null)
        {
            SearchResponse result = rshCaseRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.add(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null)
        {
            Map<String, String> searchdata = searchRequest.getCaseData().entrySet().stream().collect(Collectors.toMap(es -> es.getKey(), es-> toJson(es)));
            Set<SearchResponse> resultList = rshCaseRepository.findByNameOrDob(searchdata.get("first-name"),searchdata.get("last-name"), searchdata.get("dob"));
            results.addAll(resultList);
        }
        return results;
    }

    private String toJson(Map.Entry<String, String> entry) {

        String value = "{}";
        try {
            value = objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return value;
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

