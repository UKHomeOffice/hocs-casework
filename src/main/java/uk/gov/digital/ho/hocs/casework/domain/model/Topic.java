package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
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
    private LocalDate created;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "topic_name")
    @Getter
    private String topicName;

    @Column(name = "topic_name_uuid")
    @Getter
    private UUID topicNameUUID;

    @Column(name = "deleted")
    @Getter
    private boolean deleted = Boolean.FALSE;

    public Topic(UUID caseUUID, String topicName, UUID topicNameUUID) {
        if (caseUUID == null || topicName == null || topicNameUUID == null) {
            throw new EntityCreationException("Cannot create Topic(%s, %s, %s).", caseUUID, topicName, topicNameUUID);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDate.now();
        this.caseUUID = caseUUID;
        this.topicName = topicName;
        this.topicNameUUID = topicNameUUID;
    }

}