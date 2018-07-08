package uk.gov.digital.ho.hocs.casework.audit.model;

import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

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
    private CaseAuditEntry caseInstance;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="stage_id", referencedColumnName = "id")
    private StageAuditEntry stageInstance;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="document_id", referencedColumnName = "id")
    private DocumentAuditEntry documentInstance;

    @Column(name = "timestamp", nullable = false)
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "event_action", nullable = false)
    private final String eventAction;

    public AuditEntry(String username, CaseData caseInstance, AuditAction auditAction) {
        this.username = username;
        if(caseInstance != null) {
            this.caseInstance = CaseAuditEntry.from(caseInstance);
        }
        this.eventAction = auditAction.toString();
    }

    public AuditEntry(String username, StageData stageInstance, AuditAction auditAction) {
        this.username = username;
        if(stageInstance != null) {
            this.stageInstance = StageAuditEntry.from(stageInstance);
        }
        this.eventAction = auditAction.toString();
    }

    public AuditEntry(String username, DocumentData documentInstance, AuditAction auditAction) {
        this.username = username;
        if(documentInstance != null) {
            this.documentInstance = DocumentAuditEntry.from(documentInstance);
        }
        this.eventAction = auditAction.toString();
    }

    public AuditEntry(String username, String queryData, AuditAction auditAction) {
        this.username = username;
        this.queryData = queryData;
        this.eventAction = auditAction.toString();
    }
}
