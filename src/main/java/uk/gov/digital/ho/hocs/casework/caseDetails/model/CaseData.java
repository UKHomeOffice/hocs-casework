package uk.gov.digital.ho.hocs.casework.caseDetails.model;

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
@Getter
@NoArgsConstructor
public class CaseData implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    private Set<StageData> stages = new HashSet<>();

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    private String type;

    @Column(name = "reference")
    private String reference;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "created")
    private LocalDateTime created;

    public CaseData(String type, Long caseNumber) {
        LocalDateTime now = LocalDateTime.now();
        this.type = type;
        this.reference = String.format("%s/%07d/%s", type, caseNumber, now.format(DateTimeFormatter.ofPattern("yy")));
        this.uuid = UUID.randomUUID();
        this.created = now;
        this.stages = new HashSet<>();
    }

    }
