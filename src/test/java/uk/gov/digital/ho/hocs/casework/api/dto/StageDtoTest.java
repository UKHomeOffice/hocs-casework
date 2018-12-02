package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StageDtoTest {

    @Test
    public void getStageDtoTest() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, deadline);

        StageDto stageDto = StageDto.from(stage);

        assertThat(stageDto.getUuid()).isEqualTo(stage.getUuid());
        assertThat(stageDto.getCreated()).isEqualTo(stage.getCreated());
        assertThat(stageDto.getStageType()).isEqualTo(stage.getStageType().toString());
        assertThat(stageDto.getDeadline()).isEqualTo(stage.getDeadline());
        assertThat(stageDto.getStatus()).isEqualTo(stage.getStageStatusType().toString());
        assertThat(stageDto.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(stageDto.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(stageDto.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(stageDto.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(stageDto.getCaseType()).isEqualTo(stage.getCaseType());
        assertThat(stageDto.getData()).isEqualTo(stage.getData());

    }

    @Test
    public void getStageDtoTestNull() {

        UUID caseUUID = UUID.randomUUID();

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, null, null);

        StageDto stageDto = StageDto.from(stage);

        assertThat(stageDto.getUuid()).isEqualTo(stage.getUuid());
        assertThat(stageDto.getCreated()).isEqualTo(stage.getCreated());
        assertThat(stageDto.getStageType()).isEqualTo(stage.getStageType().toString());
        assertThat(stageDto.getDeadline()).isEqualTo(stage.getDeadline());
        assertThat(stageDto.getStatus()).isEqualTo(stage.getStageStatusType().toString());
        assertThat(stageDto.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(stageDto.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(stageDto.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(stageDto.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(stageDto.getCaseType()).isEqualTo(stage.getCaseType());
        assertThat(stageDto.getData()).isEqualTo(stage.getData());
    }
}