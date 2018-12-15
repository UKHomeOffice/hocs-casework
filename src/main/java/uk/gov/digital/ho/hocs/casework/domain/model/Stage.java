package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@NoArgsConstructor
@Entity
@Table(name = "stage")
public class Stage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String stageType;

    @Getter
    @Column(name = "deadline")
    private LocalDate deadline;

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
    @Column(name = "case_reference", insertable = false, updatable = false)
    private String caseReference;

    @Getter
    @Column(name = "case_type", insertable = false, updatable = false)
    private String caseDataType;

    @Getter
    @Column(name = "data", insertable = false, updatable = false)
    private String data;

    public Stage(UUID caseUUID, String stageType, UUID teamUUID, LocalDate deadline) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        setDeadline(deadline);
        setTeam(teamUUID);
    }


    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setTeam(UUID teamUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = null;
    }

    public void setUser(UUID userUUID) {
        this.userUUID = userUUID;
    }

}