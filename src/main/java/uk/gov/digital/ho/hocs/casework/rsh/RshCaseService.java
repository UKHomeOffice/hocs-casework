package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class RshCaseService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RshCaseRepository rshCaseRepository;

    @Autowired
    public RshCaseService(RshCaseRepository rshCaseRepository) {
        this.rshCaseRepository = rshCaseRepository;
    }

    String createRSHCase(String caseData) {

        RshCaseDetails rshCaseDetails = new RshCaseDetails("RSH",rshCaseRepository.getNextSeriesId(), caseData);
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails.getCaseReference();
    }

    String updateRSHCase(String uuid, String data) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        rshCaseDetails.setCaseData(data);
        rshCaseRepository.save(rshCaseDetails);
        return rshCaseDetails.getCaseReference();
    }

    public RshCaseDetails getRSHCase(String uuid) {
        RshCaseDetails rshCaseDetails = rshCaseRepository.findByUuid(uuid);
        return rshCaseDetails;
    }

   // public RshCaseDetails[] findCases(String data) {
//
   // }
}

