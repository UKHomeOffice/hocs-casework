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
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);

        assertThat(stage.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(stage.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(stage.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(stage.getStageType()).isEqualTo(stageType);
        assertThat(stage.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(stage.getUserUUID()).isEqualTo(null);
        assertThat(stage.getDeadline()).isEqualTo(deadline);
        assertThat(stage.getStageStatusType()).isEqualTo(StageStatusType.TEAM_ASSIGNED);

        assertThat(stage.getCaseReference()).isEqualTo(null);
        assertThat(stage.getCaseType()).isEqualTo(null);
        assertThat(stage.getData()).isEqualTo(null);
    }

    @Test(expected = EntityCreationException.class)
    public void getStageNullCaseUUID() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        new Stage(null, stageType, teamUUID, deadline);

    }

    @Test(expected = EntityCreationException.class)
    public void getStageNullType() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        new Stage(caseUUID, null, teamUUID, deadline);

    }

}