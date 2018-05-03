package uk.gov.digital.ho.hocs.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CaseService {

    private final CaseRepository caseRepository;

    @Autowired
    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public String create(CaseDetails caseDetails) {

        caseRepository.save(caseDetails);

        return caseDetails.getCaseType() + "/"
                + caseDetails.getRef() + "/"
                + caseDetails.getCaseCreated().format(DateTimeFormatter.ofPattern("yy"));


    }


}

