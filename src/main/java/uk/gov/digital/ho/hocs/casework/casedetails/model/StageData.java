package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_data")
@NoArgsConstructor
public class StageData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name ="data")
    @Getter
    @Setter
    private String data;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();


    public StageData(UUID caseUUID, String type, String data) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.data = data;
        this.caseUUID = caseUUID;
    }
}
