package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseStage implements Serializable {

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

    public void setTeam(UUID teamUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = null;
    }

    public void setUser(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public boolean isActive() {
        return this.teamUUID!=null;
    }

}
