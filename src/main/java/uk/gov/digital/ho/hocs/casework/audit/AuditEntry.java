package uk.gov.digital.ho.hocs.casework.audit;

import lombok.Getter;

import javax.persistence.*;

@Table(name = "audit")
@Getter
public class AuditEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "caseData")
    private String caseData;

    public AuditEntry(String uuid, String timestamp, String action, String username, String caseData) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.action = action;
        this.username = username;
        this.caseData = caseData;
    }
}
