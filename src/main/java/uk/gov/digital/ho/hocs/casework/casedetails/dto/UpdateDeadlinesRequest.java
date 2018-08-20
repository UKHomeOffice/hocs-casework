package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class UpdateDeadlinesRequest {

    @JsonProperty("deadlines")
    private Set<DeadlineDataDto> deadlines;
}
