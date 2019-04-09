package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "type")
    private String caseNoteType;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "text")
    private String text;

    @Getter
    @Column(name = "author")
    private String author;

    public CaseNote(UUID caseUUID, String caseNoteType, String text, String author) {
        if (caseUUID == null || caseNoteType == null || text == null) {
            throw new ApplicationExceptions.EntityCreationException(
                    String.format("Cannot create case note(%s,%s,%s).", caseUUID, caseNoteType, text), CASE_NOTE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseNoteType = caseNoteType;
        this.caseUUID = caseUUID;
        this.text = text;
        this.author = author;
    }


//TODO Migration Only Code - remove after migration
    public CaseNote(UUID caseUUID, String caseNoteType, LocalDateTime created, String text, String author) {
        if (caseUUID == null || caseNoteType == null || text == null) {
            throw new ApplicationExceptions.EntityCreationException(
                    String.format("Cannot create case note(%s,%s,%s).", caseUUID, created, text), CASE_NOTE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = created;
        this.caseNoteType = caseNoteType;
        this.caseUUID = caseUUID;
        this.text = text;
        this.author = author;
    }

}