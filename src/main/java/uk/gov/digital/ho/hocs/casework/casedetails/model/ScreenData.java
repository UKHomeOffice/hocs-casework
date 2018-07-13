package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "screen_data")
@NoArgsConstructor
public class ScreenData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "data")
    @Getter
    @Setter
    private String data;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

    public ScreenData(String type, String data, UUID stageUUID) {
        this.type = type;
        this.data = data;
        this.stageUUID = stageUUID;
    }
}
