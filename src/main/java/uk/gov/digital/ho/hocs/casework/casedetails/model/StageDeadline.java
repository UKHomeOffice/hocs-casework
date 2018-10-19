package uk.gov.digital.ho.hocs.casework.casedetails.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stage_deadline")
public class StageDeadline implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created")
    private LocalDate created;

    @Column(name = "stage_type")
    private String stageType;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "case_uuid")
    private UUID caseUUID;

    public StageDeadline(UUID caseUUID, StageType stage, LocalDate date) {
        this.created = LocalDate.now();
        this.stageType = stage.toString();
        this.date = date;
        this.caseUUID = caseUUID;
    }
}