package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateStageRequestTest {

    @Test
    public void getUpdateStageRequest() {

        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        StageStatusType stageStatusType = StageStatusType.UPDATED;

        UpdateStageRequest updateStageRequest = new UpdateStageRequest(teamUUID, userUUID, stageStatusType);

        assertThat(updateStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(updateStageRequest.getUserUUID()).isEqualTo(userUUID);
        assertThat(updateStageRequest.getStatus()).isEqualTo(stageStatusType);

    }

    @Test
    public void getUpdateStageRequestNull() {

        UpdateStageRequest updateStageRequest = new UpdateStageRequest(null, null, null);

        assertThat(updateStageRequest.getTeamUUID()).isEqualTo(null);
        assertThat(updateStageRequest.getUserUUID()).isEqualTo(null);
        assertThat(updateStageRequest.getStatus()).isEqualTo(null);

    }

}