package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_note")
public class CaseNote {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "text")
    private String text;

    public CaseNote(UUID caseUUID, CaseNoteType caseNoteType, String text) {
        if (caseUUID == null || caseNoteType == null || text == null) {
            throw new EntityCreationException("Cannot create case note(%s,%s,%s).", caseUUID, caseNoteType, text);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.type = caseNoteType.toString();
        this.caseUUID = caseUUID;
        this.text = text;
    }

    public CaseNoteType getCaseNoteType() {
        return CaseNoteType.valueOf(this.type);
    }

}