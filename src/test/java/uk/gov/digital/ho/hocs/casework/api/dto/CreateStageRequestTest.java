package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateStageRequestTest {

    @Test
    public void getCreateStageRequest() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        CreateStageRequest createStageRequest = new CreateStageRequest(stageType, teamUUID, userUUID, deadline);

        assertThat(createStageRequest.getType()).isEqualTo(stageType);
        assertThat(createStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(createStageRequest.getUserUUID()).isEqualTo(userUUID);
        assertThat(createStageRequest.getDeadline()).isEqualTo(deadline);

    }

    @Test
    public void getCreateStageRequestNull() {

        CreateStageRequest createStageRequest = new CreateStageRequest(null, null, null, null);

        assertThat(createStageRequest.getType()).isNull();
        assertThat(createStageRequest.getTeamUUID()).isNull();
        assertThat(createStageRequest.getUserUUID()).isNull();
        assertThat(createStageRequest.getDeadline()).isNull();

    }
}