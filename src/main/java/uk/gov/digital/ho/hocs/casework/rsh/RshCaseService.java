package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
public class RshCaseService {

    private final RshCaseRepository rshCaseRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RshCaseService(RshCaseRepository rshCaseRepository) {
        this.rshCaseRepository = rshCaseRepository;
    }

    public String rshCreate(String caseType, RshCaseDetails rshCaseDetails) {

        buildCaseDetails(caseType, rshCaseDetails);

        rshCaseRepository.save(rshCaseDetails);

        return rshCaseDetails.getCaseReference();


    }

    public void rshUpdate(UUID uuid, RshCaseDetails rshCaseDetails) {
        HashMap<String, Object> jsonMap = getJsonStringAsHashMap(rshCaseDetails);
        RshCaseDetails currentCaseDetails = rshCaseRepository.findByUuid(uuid);

        currentCaseDetails.setDob(LocalDate.parse(jsonMap.get("date-of-birth").toString()));
        currentCaseDetails.setForename(jsonMap.get("forename").toString());
        currentCaseDetails.setSurname(jsonMap.get("surname").toString());
        currentCaseDetails.setLastModified(LocalDateTime.now());
        currentCaseDetails.setCaseData(rshCaseDetails.getCaseData());

        rshCaseRepository.save(currentCaseDetails);

    }

    private void buildCaseDetails(String caseType, RshCaseDetails rshCaseDetails) {
        HashMap<String, Object> jsonMap = getJsonStringAsHashMap(rshCaseDetails);
        rshCaseDetails.setRef(rshCaseRepository.getNextSeriesId());
        rshCaseDetails.setUuid(UUID.randomUUID());
        rshCaseDetails.setCaseType(caseType);
        rshCaseDetails.setCaseCreated(LocalDateTime.now());
        rshCaseDetails.setLastModified(LocalDateTime.now());
        rshCaseDetails.setCaseReference(rshCaseDetails.getCaseType() + "/"
                + rshCaseDetails.getRef() + "/"
                + rshCaseDetails.getCaseCreated().format(DateTimeFormatter.ofPattern("yy")));
        rshCaseDetails.setDob(LocalDate.parse(jsonMap.get("date-of-birth").toString()));
        rshCaseDetails.setForename(jsonMap.get("forename").toString());
        rshCaseDetails.setSurname(jsonMap.get("surname").toString());
    }

    private HashMap<String, Object> getJsonStringAsHashMap(RshCaseDetails rshCaseDetails) {
        HashMap<String, Object> jsonMap = null;
        try {
            jsonMap = objectMapper.readValue(rshCaseDetails.getCaseData(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonMap;
    }


}

