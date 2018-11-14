package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
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

    public Topic(UUID caseUUID, String topicName, UUID topicNameUUID) {
        if (caseUUID == null || topicName == null || topicNameUUID == null) {
            throw new EntityCreationException("Cannot create Topic(%s, %s, %s).", caseUUID, topicName, topicNameUUID);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.text = topicName;
        this.textUUID = topicNameUUID;
    }

}