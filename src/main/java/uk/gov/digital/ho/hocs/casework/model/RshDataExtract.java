package uk.gov.digital.ho.hocs.casework.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.rsh.CaseDetails;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RshDataExtract {

    private final String caseReference;

    private final UUID uuid;

    public static RshDataExtract from(CaseDetails caseDetails) {
        return new RshDataExtract(caseDetails.getReference(), caseDetails.getUuid());
    }
}
