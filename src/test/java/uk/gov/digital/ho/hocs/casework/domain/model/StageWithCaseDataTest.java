package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StageWithCaseDataTest {

    @Test
    public void getStage() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        UUID transitionNoteUUID = UUID.randomUUID();
        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
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
        assertThat(stage.getData()).isEmpty();
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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
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
        assertThat(stage.getData()).isEmpty();

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
        assertThat(stage.getData()).isEmpty();
    }

    @Test
    public void setStageTeamNull() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
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
        assertThat(stage.getData()).isEmpty();

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
        assertThat(stage.getData()).isEmpty();
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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
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
        assertThat(stage.getData()).isEmpty();

        stage.setUserUUID(newuserUUID);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(newuserUUID);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEmpty();
    }

    @Test
    public void setStageUserNull() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
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
        assertThat(stage.getData()).isEmpty();

        stage.setUserUUID(null);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseDataType()).isEqualTo(null);
        assertThat(stage.getData()).isEmpty();
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getStageNullCaseUUID() {
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        UUID transitionNoteUUID = UUID.randomUUID();
        new StageWithCaseData(null, stageType, teamUUID, userUUID, transitionNoteUUID);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getStageNullType() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        new StageWithCaseData(caseUUID, null, teamUUID, userUUID, transitionNoteUUID);

    }

}
