package uk.gov.digital.ho.hocs.casework.caseDetails.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<StageData> stages = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<DocumentData> documentData = new HashSet<>();

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    @Setter
    private String type;

    @Column(name = "reference")
    @Getter
    private String reference;

    @Column(name = "uuid")
    @Getter
    private UUID uuid = UUID.randomUUID();

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    public CaseData(String type, Long caseNumber) {
        this.type = type;
        this.reference = String.format("%s/%07d/%s", type, caseNumber, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy")));
    }

}
