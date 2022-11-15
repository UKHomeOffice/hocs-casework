package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "stage")
@NoArgsConstructor
public class BaseStage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Getter
    @Setter
    @Column(name = "uuid")
    protected UUID uuid;

    @Getter
    @Column(name = "created")
    protected LocalDateTime created;

    @Getter
    @Column(name = "type")
    protected String stageType;

    @Getter
    @Setter
    @Column(name = "deadline")
    protected LocalDate deadline;

    @Getter
    @Setter
    @Column(name = "transition_note_uuid")
    protected UUID transitionNoteUUID;

    @Getter
    @Column(name = "case_uuid")
    protected UUID caseUUID;

    @Getter
    @Column(name = "team_uuid")
    protected UUID teamUUID;

    @Getter
    @Setter
    @Column(name = "user_uuid")
    protected UUID userUUID;

    @Getter
    @Setter
    @Column(name = "deadline_warning")
    protected LocalDate deadlineWarning;

    @Getter
    @Setter
    @Column(name = "somu", insertable = false, updatable = false)
    protected String somu;

    public BaseStage(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, UUID transitionNoteUUID) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        this.transitionNoteUUID = transitionNoteUUID;
        setTeam(teamUUID);
        this.userUUID = userUUID;
    }

    public void setTeam(UUID teamUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = null;
    }

    public void setUser(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public boolean isActive() {
        return this.teamUUID != null;
    }

}
