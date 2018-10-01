package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;

import java.util.UUID;


@AllArgsConstructor
@Getter
public class GetCorrespondentResponse {

    @JsonProperty("value")
    private UUID value;

    @JsonProperty("label")
    private String label;

    public static GetCorrespondentResponse from(CorrespondentData correspondentData, CorrespondentType correspondentType) {
        return new GetCorrespondentResponse(correspondentData.getUuid(), correspondentData.getFullName());
    }
}
