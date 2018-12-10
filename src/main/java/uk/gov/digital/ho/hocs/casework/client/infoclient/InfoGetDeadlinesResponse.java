package uk.gov.digital.ho.hocs.casework.client.infoclient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@NoArgsConstructor
@Getter
public class InfoGetDeadlinesResponse {

    @JsonProperty("deadlines")
    private Map<String, LocalDate> deadlines;
}