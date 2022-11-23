package uk.gov.digital.ho.hocs.casework.domain.model.workstacks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "WorkstackActiveStage")
@Table(name = "active_stage")
public class ActiveStage implements Serializable {

    @Id
    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String stageType;

    @Setter
    @Getter
    @Column(name = "deadline")
    private LocalDate deadline;

    @Setter
    @Getter
    @Column(name = "deadlineWarning")
    private LocalDate deadlineWarning;

    @Getter
    @Column(name = "transition_note_uuid", columnDefinition = "uuid")
    private UUID transitionNoteUUID;

    @Getter
    @Column(name = "case_uuid", columnDefinition = "uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "team_uuid", columnDefinition = "uuid")
    private UUID teamUUID;

    @Getter
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUUID;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private CaseData caseData;

    @Getter
    @Setter
    @Transient
    private String dueContribution;

    @Getter
    @Setter
    @Transient
    private String contributions;

    @Getter
    @Setter
    @Transient
    @JsonInclude
    private String nextCaseType;

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        ActiveStage that = (ActiveStage) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}