package uk.gov.digital.ho.hocs.casework.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class MigrationCreateCaseNoteRequest {

    @JsonProperty(value="date")
    private LocalDateTime date;

//    @NotEmpty
    @JsonProperty(value="user")
    private String user;

//    @NotEmpty
    @JsonProperty(value="text")
    private String text;

}