package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "case_data")
@NoArgsConstructor
public class CaseData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    private String caseType;

    @Column(name = "reference")
    @Getter
    private String reference;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<StageData> stages = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<DocumentData> documents = new HashSet<>();

    public CaseData(CaseType caseType, Long caseNumber) {
        this.uuid = UUID.randomUUID();
        this.caseType = caseType.toString();
        this.reference = String.format("%s/%07d/%s", caseType.toString(), caseNumber, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy")));
    }

    public CaseType getType() {
        return CaseType.valueOf(this.caseType);
    }

}
