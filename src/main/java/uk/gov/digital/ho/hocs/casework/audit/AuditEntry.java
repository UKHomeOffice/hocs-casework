package uk.gov.digital.ho.hocs.casework.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.model.CorrelationDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit")
@Getter
public class AuditEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "case_uuid", nullable = false)
    private UUID caseUUID;

    @Column(name = "case_Stage", nullable = false)
    private String caseStage;

    @Column(name = "event_uuid", nullable = false)
    private UUID eventUUID;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "event_action", nullable = false)
    private String eventAction;

    @Column(name = "event_data")
    private String eventData;

    public AuditEntry(CorrelationDetails correlationDetails,
                      UUID caseUUID,
                      String caseStage,
                      AuditAction eventAction,
                      String caseData) {
        this.username = correlationDetails.getUserName();
        this.eventUUID = correlationDetails.getCorrelationID();
        this.eventTimestamp = correlationDetails.getTimestamp();
        this.caseUUID = caseUUID;
        this.caseStage = caseStage;
        this.eventAction = eventAction.toString();
        this.eventData = caseData;


    }
}
