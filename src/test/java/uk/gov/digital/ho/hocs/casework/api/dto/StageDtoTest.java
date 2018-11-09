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
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, userUUID, deadline);

        StageDto stageDto = StageDto.from(stage);

        assertThat(stageDto.getUuid()).isEqualTo(stage.getUuid());
        assertThat(stageDto.getCreated()).isEqualTo(stage.getCreated());
        assertThat(stageDto.getStageType()).isEqualTo(stage.getStageType().toString());
        assertThat(stageDto.getDeadline()).isEqualTo(stageDto.getDeadline());
        assertThat(stageDto.getStatus()).isEqualTo(stageDto.getStatus());
        assertThat(stageDto.getCaseUUID()).isEqualTo(stageDto.getCaseUUID());
        assertThat(stageDto.getTeamUUID()).isEqualTo(stageDto.getTeamUUID());
        assertThat(stageDto.getUserUUID()).isEqualTo(stageDto.getUserUUID());
        assertThat(stageDto.getCaseReference()).isEqualTo(stageDto.getCaseReference());
        assertThat(stageDto.getCaseType()).isEqualTo(stageDto.getCaseType());
        assertThat(stageDto.getData()).isEqualTo(stageDto.getData());

    }

    @Test
    public void getStageDtoTestNull() {

        UUID caseUUID = UUID.randomUUID();

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, null, null, null);

        StageDto stageDto = StageDto.from(stage);

        assertThat(stageDto.getUuid()).isEqualTo(stage.getUuid());
        assertThat(stageDto.getCreated()).isEqualTo(stage.getCreated());
        assertThat(stageDto.getStageType()).isEqualTo(stage.getStageType().toString());
        assertThat(stageDto.getDeadline()).isEqualTo(stageDto.getDeadline());
        assertThat(stageDto.getStatus()).isEqualTo(stageDto.getStatus());
        assertThat(stageDto.getCaseUUID()).isEqualTo(stageDto.getCaseUUID());
        assertThat(stageDto.getTeamUUID()).isEqualTo(stageDto.getTeamUUID());
        assertThat(stageDto.getUserUUID()).isEqualTo(stageDto.getUserUUID());
        assertThat(stageDto.getCaseReference()).isEqualTo(stageDto.getCaseReference());
        assertThat(stageDto.getCaseType()).isEqualTo(stageDto.getCaseType());
        assertThat(stageDto.getData()).isEqualTo(stageDto.getData());
    }
}