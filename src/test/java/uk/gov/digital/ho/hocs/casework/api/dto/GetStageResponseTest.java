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
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();
        UUID transitionNoteUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        stage.setDeadline(deadline);
        stage.setDeadlineWarning(LocalDate.now());
        stage.setAssignedTopic("Mock assigned topic");

        GetStageResponse getStageResponse = GetStageResponse.from(stage);

        assertThat(getStageResponse.getUuid()).isEqualTo(stage.getUuid());
        assertThat(getStageResponse.getCreated()).isEqualTo(stage.getCreated());
        assertThat(getStageResponse.getStageType()).isEqualTo(stage.getStageType());
        assertThat(getStageResponse.getDeadline()).isEqualTo(stage.getDeadline());
        assertThat(getStageResponse.getDeadlineWarning()).isEqualTo(stage.getDeadlineWarning());
        assertThat(getStageResponse.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(getStageResponse.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(getStageResponse.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(getStageResponse.getTransitionNoteUUID()).isEqualTo(stage.getTransitionNoteUUID());
        assertThat(getStageResponse.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(getStageResponse.getCaseDataType()).isEqualTo(stage.getCaseDataType());
        assertThat(getStageResponse.getData()).isEqualTo(stage.getData());
        assertThat(getStageResponse.getAssignedTopic()).isEqualTo(stage.getAssignedTopic());

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
        assertThat(getStageResponse.getDeadlineWarning()).isEqualTo(stage.getDeadlineWarning());
        assertThat(getStageResponse.getCaseUUID()).isEqualTo(stage.getCaseUUID());
        assertThat(getStageResponse.getTeamUUID()).isEqualTo(stage.getTeamUUID());
        assertThat(getStageResponse.getUserUUID()).isEqualTo(stage.getUserUUID());
        assertThat(getStageResponse.getTransitionNoteUUID()).isEqualTo(stage.getTransitionNoteUUID());
        assertThat(getStageResponse.getCaseReference()).isEqualTo(stage.getCaseReference());
        assertThat(getStageResponse.getCaseDataType()).isEqualTo(stage.getCaseDataType());
        assertThat(getStageResponse.getData()).isEqualTo(stage.getData());
    }
}
