package uk.gov.digital.ho.hocs.casework.audit.model;

import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="document_id", referencedColumnName = "id")
    private DocumentAudit documentInstance;

    @Column(name = "timestamp", nullable = false)
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "event_action", nullable = false)
    private final String eventAction;

    public AuditEntry(String username, CaseData caseInstance, EventAction eventAction) {
        this.username = username;
        if(caseInstance != null) {
            this.caseInstance = CaseDataAudit.from(caseInstance);
        }
        this.eventAction = eventAction.toString();
    }

    public AuditEntry(String username, StageData stageInstance, EventAction eventAction) {
        this.username = username;
        if(stageInstance != null) {
            this.stageInstance = StageDataAudit.from(stageInstance);
        }
        this.eventAction = eventAction.toString();
    }

    public AuditEntry(String username, DocumentData documentInstance, EventAction eventAction) {
        this.username = username;
        if(documentInstance != null) {
            this.documentInstance = DocumentAudit.from(documentInstance);
        }
        this.eventAction = eventAction.toString();
    }

    public AuditEntry(String username, String queryData, EventAction eventAction) {
        this.username = username;
        this.queryData = queryData;
        this.eventAction = eventAction.toString();
    }
}
