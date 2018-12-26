package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetStageResponseTest {

    @Test
    public void getStageDtoTest() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        UUID transitionNoteUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline, transitionNoteUUID);

        GetStageResponse getStageResponse = GetStageResponse.from(stage);

        assertThat(getStageResponse.getUuid()).isEqualTo(stage.getUuid());
        assertThat(getStageResponse.getCreated()).isEqualTo(stage.getCreated());
        assertThat(getStageResponse.getStageType()).isEqualTo(stage.getStageType());
        assertThat(getStageResponse.getDeadline()).isEqualTo(stage.getDeadline());
        assertThat(getStageResponse.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(getStageResponse.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(getStageResponse.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(getStageResponse.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(getStageResponse.getCaseDataType()).isEqualTo(stage.getCaseDataType());
        assertThat(getStageResponse.getData()).isEqualTo(stage.getData());

    }

    @Test
    public void getStageDtoTestNull() {

        UUID caseUUID = UUID.randomUUID();

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", null, null, null);

        GetStageResponse getStageResponse = GetStageResponse.from(stage);

        assertThat(getStageResponse.getUuid()).isEqualTo(stage.getUuid());
        assertThat(getStageResponse.getCreated()).isEqualTo(stage.getCreated());
        assertThat(getStageResponse.getStageType()).isEqualTo(stage.getStageType());
        assertThat(getStageResponse.getDeadline()).isEqualTo(stage.getDeadline());
        assertThat(getStageResponse.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(getStageResponse.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(getStageResponse.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(getStageResponse.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(getStageResponse.getCaseDataType()).isEqualTo(stage.getCaseDataType());
        assertThat(getStageResponse.getData()).isEqualTo(stage.getData());
    }
}