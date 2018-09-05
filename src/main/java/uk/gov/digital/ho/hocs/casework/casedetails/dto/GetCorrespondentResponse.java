package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateCorrespondentRequest;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetCorrespondentResponse {

    @JsonProperty("correspondents")
    Set<CreateCorrespondentRequest> correspondents;

    public static GetCorrespondentResponse from(Set<CorrespondentData> correspondentData) {
        Set<CreateCorrespondentRequest> correspondents = correspondentData
                .stream()
                .map(cd -> CreateCorrespondentRequest.from(cd, CorrespondentType.APPLICANT))
                .collect(Collectors.toSet());

        return new GetCorrespondentResponse(correspondents);
    }
}
