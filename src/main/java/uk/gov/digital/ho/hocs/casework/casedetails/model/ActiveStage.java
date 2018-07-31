package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "active_stage")
@NoArgsConstructor
public class ActiveStage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_type")
    @Getter
    private String caseType;

    @Column(name = "case_reference")
    @Getter
    private String caseReference;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "stage_type")
    @Getter
    private String type;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

    public ActiveStage(UUID stageUUID, String stageType, UUID caseUUID, String caseReference, String caseType) {
        this.stageUUID = stageUUID;
        this.type = stageType;
        this.caseUUID = caseUUID;
        this.caseReference = caseReference;
        this.caseType = caseType;
    }
}
