package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.TOPIC_CREATE_FAILED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "topic")
public class Topic implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "text_uuid")
    @Getter
    private UUID textUUID;

    @Setter
    @Getter
    @Column(name = "deleted")
    private boolean deleted;

    public Topic(UUID caseUUID, String topicName, UUID topicNameUUID) {
        if (caseUUID==null || topicName==null || topicNameUUID==null) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot create Topic(%s, %s, %s).", caseUUID, topicName, topicNameUUID),
                TOPIC_CREATE_FAILED);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.text = topicName;
        this.textUUID = topicNameUUID;
    }

}