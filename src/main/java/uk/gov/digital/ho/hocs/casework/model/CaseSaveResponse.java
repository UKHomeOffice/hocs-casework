package uk.gov.digital.ho.hocs.casework.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseSaveResponse {

    private final String caseReference;

    private final UUID uuid;

    public static CaseSaveResponse from(CaseDetails caseDetails) {
        return new CaseSaveResponse(caseDetails.getReference(), caseDetails.getUuid());
    }
}
