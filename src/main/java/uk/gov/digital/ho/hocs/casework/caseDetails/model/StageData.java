package uk.gov.digital.ho.hocs.casework.caseDetails.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_data")
public class StageData implements Serializable {

    @Column(name = "type")
    @Getter
    @Setter
    private String type;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="uuid")
    @Getter
    private UUID uuid = UUID.randomUUID();

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name ="data")
    @Getter
    @Setter
    private String data;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    public StageData(UUID caseUUID, String type, String data) {
        this.type = type;
        this.data = data;
        this.caseUUID = caseUUID;
    }
}
