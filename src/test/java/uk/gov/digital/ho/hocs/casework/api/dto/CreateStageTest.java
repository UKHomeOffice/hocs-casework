package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateStageTest {

    @Test
    public void getCreateStageResponse() {

        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        CreateStageResponse createStageResponse = CreateStageResponse.from(stage);

        assertThat(createStageResponse.getUuid()).isEqualTo(stage.getUuid());

    }

}
