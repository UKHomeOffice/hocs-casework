package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "topic")
public class Topic implements Serializable {

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

    @Column(name = "topic_uuid")
    @Getter
    private UUID topicUUID;

    public Topic(UUID caseUUID, String text, UUID topicUUID) {
        if (caseUUID == null || text == null || topicUUID == null) {
            throw new EntityCreationException("Cannot create Topic(%s, %s, %s).", caseUUID, text, topicUUID);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.text = text;
        this.topicUUID = topicUUID;
    }

}