package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.model.StageDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "rsh_case")
@Getter
@NoArgsConstructor
public class CaseDetails implements Serializable {

    public CaseDetails(String type, Long caseNumber, StageDetails stageDetails) {
        LocalDateTime now = LocalDateTime.now();
        this.caseType = type;
        this.caseReference = String.format("%s/%07d/%s", type, caseNumber,now.format(DateTimeFormatter.ofPattern("yy")));
        this.uuid = UUID.randomUUID().toString();
        this.caseCreated = now;
        this.caseData = caseData;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    private String caseType;

    @Column(name = "reference")
    private String caseReference;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "created")
    private LocalDateTime caseCreated;

    @Column(name = "data")
    @Setter
    private String caseData;
}
