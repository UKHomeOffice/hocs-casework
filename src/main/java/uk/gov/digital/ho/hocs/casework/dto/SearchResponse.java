package uk.gov.digital.ho.hocs.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor()
@Getter
@Setter
public class SearchResponse {

    private String caseReference;

    private String uuid;

    @JsonProperty("first-name")
    private String firstName;

    @JsonProperty("last-name")
    private String lastName;

    @JsonProperty("date-of-birth")
    private String dateOfBirth;

    @JsonProperty("outcome")
    private String outcome;

    private String caseData;
}
