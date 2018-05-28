package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CorrelationDetails {

    private UUID correlationID;

    private LocalDateTime timestamp;

    private String userName;


    public CorrelationDetails(String userName) {
        this.correlationID = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.userName = userName;
    }
}
