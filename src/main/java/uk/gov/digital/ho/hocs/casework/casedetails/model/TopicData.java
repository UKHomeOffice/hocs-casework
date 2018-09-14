package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "topic_data")
@NoArgsConstructor
public class TopicData implements Serializable {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_uuid")
    @Getter
    @Setter
    private UUID caseUUID;

    @Column(name = "topic_name")
    @Getter
    private String topicName;

    @Column(name = "topic_uuid")
    @Getter
    @Setter
    private UUID topicUUID;

    @Column(name = "created")
    @Getter
    private LocalDate created;

    @Getter
    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    public TopicData(UUID caseUUID, String topicName, UUID topicUUID) {
        this.caseUUID = caseUUID;
        this.topicName = topicName;
        this.topicUUID = topicUUID;
        this.created = LocalDate.now();
    }

    public TopicData(UUID caseUUID, UUID topicUUID) {
        this.caseUUID = caseUUID;
        this.topicUUID = topicUUID;
        this.created = LocalDate.now();
    }

    public void delete(){
        this.deleted = Boolean.TRUE;
    }


    public void reAdd() {
        this.deleted = Boolean.FALSE;
        this.created = LocalDate.now();
    }
}
