package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@NoArgsConstructor
@Entity
@Table(name = "stage")
public class Stage extends BaseStage {

    public Stage(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, UUID transitionNoteUUID) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        this.transitionNoteUUID = transitionNoteUUID;
        setTeam(teamUUID);
        this.userUUID = userUUID;
    }

}
