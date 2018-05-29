package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.caseDetails.StageDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "case_details")
@Getter
@NoArgsConstructor
public class CaseDetails implements Serializable {

    public CaseDetails(String type, Long caseNumber) {
        LocalDateTime now = LocalDateTime.now();
        this.type = type;
        this.reference = String.format("%s/%07d/%s", type, caseNumber,now.format(DateTimeFormatter.ofPattern("yy")));
        this.uuid = UUID.randomUUID();
        this.created = now;
        this.stages = new HashSet<>();
    }

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name ="case_uuid", referencedColumnName = "uuid")
    private Set<StageDetails> stages = new HashSet<>();

    }
