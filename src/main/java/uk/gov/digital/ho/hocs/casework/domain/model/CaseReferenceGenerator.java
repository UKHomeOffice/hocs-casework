package uk.gov.digital.ho.hocs.casework.domain.model;

import java.time.LocalDateTime;

public class CaseReferenceGenerator {

    private CaseReferenceGenerator() {

    }

    public static String generateCaseReference(String type, Long caseNumber, LocalDateTime createdDateTime) {
        return String.format("%S/%07d/%ty", type, caseNumber, createdDateTime);
    }
}
