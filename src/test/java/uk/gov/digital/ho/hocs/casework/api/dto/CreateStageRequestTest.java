package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateStageRequestTest {

    @Test
    public void getCreateStageRequest() {

        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        String allocate = "ALLOCATE_INDIVIDUAL";
        UUID transitionNoteUUID = UUID.randomUUID();

        CreateStageRequest createStageRequest = new CreateStageRequest(stageType, null, teamUUID, allocate,
            transitionNoteUUID, userUUID);

        assertThat(createStageRequest.getType()).isEqualTo(stageType);
        assertThat(createStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(createStageRequest.getUserUUID()).isEqualTo(userUUID);
        assertThat(createStageRequest.getAllocationType()).isEqualTo(allocate);
        assertThat(createStageRequest.getTransitionNoteUUID()).isEqualTo(transitionNoteUUID);

    }

    @Test
    public void getCreateStageRequestNull() {

        CreateStageRequest createStageRequest = new CreateStageRequest(null, null, null, null, null, null);

        assertThat(createStageRequest.getType()).isNull();
        assertThat(createStageRequest.getTeamUUID()).isNull();
        assertThat(createStageRequest.getUserUUID()).isNull();
        assertThat(createStageRequest.getAllocationType()).isNull();
        assertThat(createStageRequest.getTransitionNoteUUID()).isNull();

    }

}
