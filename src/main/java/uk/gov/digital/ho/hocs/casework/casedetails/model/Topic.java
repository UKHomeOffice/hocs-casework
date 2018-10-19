package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

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

    @Column(name = "topic_uuid")
    @Getter
    private UUID topicUUID;

    @Column(name = "deleted")
    @Getter
    private boolean deleted = Boolean.FALSE;

    public Topic(UUID caseUUID, String topicName, UUID topicUUID) {
        if (caseUUID == null || topicName == null || topicUUID == null) {
            throw new EntityCreationException("Cannot create Topic(%s, %s, %s).", caseUUID, topicName, topicUUID);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDate.now();
        this.caseUUID = caseUUID;
        this.topicName = topicName;
        this.topicUUID = topicUUID;
    }

    public void delete() {
        this.deleted = Boolean.TRUE;

    }
}