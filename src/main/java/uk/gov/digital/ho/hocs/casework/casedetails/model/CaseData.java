package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    private Set<StageData> stages = new HashSet<>();

    @Getter
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    private Set<DocumentData> documents = new HashSet<>();

    @Getter
    @Setter
    @Transient
    private InputData inputData;

    public CaseData(CaseType type, Long caseNumber) {
        if (type == null || caseNumber == null) {
            throw new EntityCreationException("Cannot create InputData(%s,%s).", type, caseNumber);
        }
        this.created = LocalDateTime.now();
        this.uuid = UUID.randomUUID();
        this.type = type.toString();
        this.reference = String.format("%S/%07d/%ty", this.type, caseNumber, this.created);

    }

    public CaseType getType() {
        return CaseType.valueOf(this.type);
    }

    public String getTypeString() {
        return this.type;
    }
}