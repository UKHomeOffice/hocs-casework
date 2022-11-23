package uk.gov.digital.ho.hocs.casework.domain.model.workstacks;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "WorkstackTopic")
@Table(name = "topic")
public class Topic implements Serializable {

    @Id
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

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        Topic topic = (Topic) o;
        return uuid.equals(topic.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}