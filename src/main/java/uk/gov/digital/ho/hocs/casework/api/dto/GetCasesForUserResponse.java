package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCasesForUserResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    public static GetCasesForUserResponse from(CaseData caseData) {

        return new GetCasesForUserResponse(caseData.getUuid());
    }

}
