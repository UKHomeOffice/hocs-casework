package uk.gov.digital.ho.hocs.casework.audit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "audit_case_data")
public class CaseDataAudit implements Serializable {

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<StageDataAudit> stages = new HashSet<>();

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "reference")
    @Getter
    private String reference;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    private CaseDataAudit(String type, String reference, UUID uuid, LocalDateTime timestamp, Set<StageDataAudit> stages) {
        this.type = type;
        this.reference = reference;
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.stages = stages;
    }

    public static CaseDataAudit from(CaseData caseData) {
        Set<StageDataAudit> stageAudits = caseData.getStages().stream().map(StageDataAudit::from).collect(Collectors.toSet());
        return new CaseDataAudit(caseData.getType(), caseData.getReference(), caseData.getUuid(), caseData.getTimestamp(), stageAudits);
    }

}
