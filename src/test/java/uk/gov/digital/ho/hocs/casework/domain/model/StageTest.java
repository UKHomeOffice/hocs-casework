package uk.gov.digital.ho.hocs.casework.domain.model;


import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StageTest {

    @Test
    public void getStage() {

        UUID caseUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(userUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);
        assertThat(stage.getStageStatusType()).isEqualTo(StageStatusType.CREATED);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test(expected = EntityCreationException.class)
    public void getStageNullCaseUUID() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        StageStatusType stageStatusType = StageStatusType.CREATED;

        new Stage(null, stageType, teamUUID, userUUID, deadline);

    }

    @Test(expected = EntityCreationException.class)
    public void getStageNullType() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        StageStatusType stageStatusType = StageStatusType.CREATED;

        new Stage(caseUUID, null, teamUUID, userUUID, deadline);

    }

    public void update() {

        UUID caseUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

        UUID newTeamUUID = UUID.randomUUID();
        UUID newUserUUID = UUID.randomUUID();
        StageStatusType newStageStatusType = StageStatusType.CREATED;

        stage.update(newTeamUUID, newUserUUID, newStageStatusType);

        assertThat(stage.getTeamUUID()).isEqualTo(newTeamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(newUserUUID);
        assertThat(stage.getStageStatusType()).isEqualTo(newStageStatusType);

    }

    @Test(expected = EntityCreationException.class)
    public void updateNoStageStatusType() {

        UUID caseUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

        UUID newTeamUUID = UUID.randomUUID();
        UUID newUserUUID = UUID.randomUUID();

        stage.update(newTeamUUID, newUserUUID, null);

    }
}