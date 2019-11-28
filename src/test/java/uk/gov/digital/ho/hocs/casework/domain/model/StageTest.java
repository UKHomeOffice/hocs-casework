package uk.gov.digital.ho.hocs.casework.domain.model;


import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StageTest {

    @Test
    public void getStage() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        UUID transitionNoteUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(userUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);
        assertThat(stage.getTransitionNoteUUID()).isEqualTo(transitionNoteUUID);
        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test
    public void setStageTeam() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID newTeamUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(userUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);

        stage.setTeam(newTeamUUID);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(newTeamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test
    public void setStageTeamNull() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(userUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);

        stage.setTeam(null);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(null);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test
    public void setStageUser() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = null;
        UUID newuserUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);

        stage.setUser(newuserUUID);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(newuserUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test
    public void setStageUserNull() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(userUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);

        stage.setUser(null);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getStageNullCaseUUID() {

        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        UUID transitionNoteUUID = UUID.randomUUID();
        new Stage(null, stageType, teamUUID, userUUID, transitionNoteUUID);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getStageNullType() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        new Stage(caseUUID, null, teamUUID, userUUID, transitionNoteUUID);

    }

}