package uk.gov.digital.ho.hocs.casework.audit;

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

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Column(name = "case_Stage")
    private String caseStage;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "event_action", nullable = false)
    private String eventAction;

    @Column(name = "event_data")
    private String eventData;

    public AuditEntry(String username,
                      UUID caseUUID,
                      String caseStage,
                      AuditAction eventAction,
                      String caseData) {
        this.username = username;
        this.caseUUID = caseUUID;
        this.caseStage = caseStage;
        this.timestamp = LocalDateTime.now();
        this.eventAction = eventAction.toString();
        this.eventData = caseData;
    }
}
