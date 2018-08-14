package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    @Transient
    @Getter
    @Setter
    private CaseInputData caseInputData;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<StageData> stages = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Getter
    private Set<DocumentData> documents = new HashSet<>();

    public CaseData() {
        this.timestamp = LocalDateTime.now();
        this.uuid = UUID.randomUUID();
    }
}