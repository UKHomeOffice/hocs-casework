package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deadline_data")
@NoArgsConstructor
public class DeadlineData implements Serializable {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_uuid")
    @Getter
    @Setter
    private UUID caseUUID;

    @Column(name = "stage")
    @Getter
    private String stage;

    @Column(name = "date")
    @Getter
    private LocalDate date;

    public DeadlineData(UUID caseUUID, StageType stage, LocalDate date) {
        this.caseUUID = caseUUID;
        this.date = date;
        this.stage = stage.toString();
    }


    public void update(LocalDate date, StageType stage) {
        if (date == null || stage == null) {
            throw new EntityCreationException("Cannot call DeadlineData.update(%s, %s) for case ref %s.", date, stage, caseUUID);
        }
        this.date = date;
        this.stage = stage.toString();
    }
}
