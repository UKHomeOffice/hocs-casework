package uk.gov.digital.ho.hocs.casework.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

    @Column(name = "case_username", nullable = false)
    private String username;

    @Column(name = "case_uuid", nullable = false)
    private String caseUUID;

    @Column(name = "case_type", nullable = false)
    private String caseType;

    @Column(name = "case_Stage", nullable = false)
    private String caseStage;

    @Column(name = "event_uuid", nullable = false)
    private String eventUUID;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "event_action", nullable = false)
    private String eventAction;


    @Column(name = "event_data")
    private String eventData;

    public AuditEntry(String username,
                      UUID caseUUID,
                      String caseType,
                      String caseStage,
                      UUID eventUUID,
                      LocalDateTime eventTimestamp,
                      AuditAction eventAction,
                      String caseData) {
        this.username = username;
        this.caseType = caseType;
        this.caseUUID = caseUUID.toString();
        this.caseStage = caseStage;
        this.eventUUID = eventUUID.toString();
        this.eventTimestamp = eventTimestamp;
        this.eventAction = eventAction.toString();
        this.eventData = caseData;


    }
}
