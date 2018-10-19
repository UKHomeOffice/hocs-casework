package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_note")
public class CaseNote implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "created")
    @Getter
    private LocalDateTime created;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "text")
    @Getter
    private String text;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "deleted")
    @Getter
    private boolean deleted = Boolean.FALSE;

    public CaseNote(UUID caseUUID, CaseNoteType caseNoteType, String text) {
        if (caseUUID == null || caseNoteType == null || text == null) {
            throw new EntityCreationException("Cannot create case note(%s,%s,%s).", caseUUID, caseNoteType, text);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.type = caseNoteType.toString();
        this.text = text;
    }

    public CaseNoteType getCaseNoteType() {
        return CaseNoteType.valueOf(this.type);
    }

    public void delete() {
        this.deleted = Boolean.TRUE;
    }
}