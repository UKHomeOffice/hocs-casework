package uk.gov.digital.ho.hocs.casework.audit.model;

import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit")
public class AuditEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private final String username;

    @Column(name = "query_data")
    private String queryData;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="case_id", referencedColumnName = "id")
    private CaseDataAudit caseInstance;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="stage_id", referencedColumnName = "id")
    private StageDataAudit stageInstance;

    @Column(name = "timestamp", nullable = false)
    private final LocalDateTime timestamp;

    @Column(name = "event_action", nullable = false)
    private final String eventAction;

    public AuditEntry(String username, CaseData caseInstance, StageData stageInstance, AuditAction eventAction) {
        this.username = username;
        if(caseInstance != null) {
            this.caseInstance = CaseDataAudit.from(caseInstance);
        }
        if(stageInstance != null) {
            this.stageInstance = StageDataAudit.from(stageInstance);
        }
        this.timestamp = LocalDateTime.now();
        this.eventAction = eventAction.toString();
    }

    public AuditEntry(String username, String queryData, AuditAction eventAction) {
        this.username = username;
        this.queryData = queryData;
        this.timestamp = LocalDateTime.now();
        this.eventAction = eventAction.toString();
    }
}
