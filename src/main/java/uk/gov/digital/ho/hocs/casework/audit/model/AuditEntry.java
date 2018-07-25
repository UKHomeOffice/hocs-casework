package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;
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
    @Getter
    private final String username;
    @Column(name = "event_action", nullable = false)
    @Getter
    private final String eventAction;

    @Column(name = "timestamp", nullable = false)
    private final LocalDateTime timestamp = LocalDateTime.now();
    @Column(name = "query_data")
    @Getter
    private String queryData;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="case_id", referencedColumnName = "id")
    @Getter
    private CaseAuditEntry caseInstance;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="stage_id", referencedColumnName = "id")
    @Getter
    private StageAuditEntry stageInstance;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="document_id", referencedColumnName = "id")
    @Getter
    private DocumentAuditEntry documentInstance;

    public AuditEntry(String username, AuditAction auditAction) {
        this.username = username;
        this.eventAction = auditAction.toString();
    }

    public AuditEntry(String username, CaseData caseInstance, AuditAction auditAction) {
        this(username, auditAction);
        this.caseInstance = CaseAuditEntry.from(caseInstance);
    }

    public AuditEntry(String username, StageData stageInstance, AuditAction auditAction) {
        this(username, auditAction);
        this.stageInstance = StageAuditEntry.from(stageInstance);
    }

    public AuditEntry(String username, DocumentData documentInstance, AuditAction auditAction) {
        this(username, auditAction);
        this.documentInstance = DocumentAuditEntry.from(documentInstance);
    }

    public AuditEntry(String username, String queryData, AuditAction auditAction) {
        this(username, auditAction);
        this.queryData = queryData;
    }
}
