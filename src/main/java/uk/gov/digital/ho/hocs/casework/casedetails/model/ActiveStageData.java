package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "active_stage_data")
@NoArgsConstructor
public class ActiveStageData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "stage_type")
    @Getter
    private String type;

    @Column(name = "screen_data")
    @Getter
    @Setter
    private String data;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

    public ActiveStageData(String type, String data, UUID stageUUID) {
        this.type = type;
        this.data = data;
        this.stageUUID = stageUUID;
    }
}
