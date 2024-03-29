package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_CREATE_FAILURE;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "case_note")
public class CaseNote implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Setter
    @Column(name = "type")
    private String caseNoteType;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Setter
    @Column(name = "text")
    private String text;

    @Getter
    @Column(name = "author")
    private String author;

    @Getter
    @Setter
    @Column(name = "deleted")
    private Boolean deleted;

    @Getter
    @Setter
    @Column(name = "edited")
    private LocalDateTime edited;

    @Getter
    @Setter
    @Column(name = "editor")
    private String editor;

    public CaseNote(UUID caseUUID, String caseNoteType, String text, String author) {
        if (caseUUID==null || caseNoteType==null || text==null) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot create case note(%s,%s,%s).", caseUUID, caseNoteType, text),
                CASE_NOTE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseNoteType = caseNoteType;
        this.caseUUID = caseUUID;
        this.text = text;
        this.author = author;
        this.deleted = false;
    }

}
