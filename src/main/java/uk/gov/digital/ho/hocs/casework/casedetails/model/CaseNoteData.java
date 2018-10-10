package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_note_data")
@Where(clause = "not deleted")
@NoArgsConstructor
@AllArgsConstructor
public class CaseNoteData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid")
    @Getter
    @Setter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    @Setter
    private UUID caseUUID;

    @Column(name = "case_note")
    @Getter
    private String caseNote;

    @Column(name = "created")
    @Getter
    private LocalDateTime created;

    @Getter
    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    public CaseNoteData(UUID caseUUID, String caseNote) {
        if (caseUUID == null || caseNote == null) {
            throw new EntityCreationException("Cannot create case note(%s,%s).", caseUUID, caseNote);
        }
        this.uuid = UUID.randomUUID();
        this.caseUUID = caseUUID;
        this.caseNote = caseNote;
        this.created = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = Boolean.TRUE;
    }

}
