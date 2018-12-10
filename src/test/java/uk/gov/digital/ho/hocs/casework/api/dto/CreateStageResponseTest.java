package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateStageResponseTest {

    @Test
    public void getCreateStageRequest() {


        UUID caseUUID = UUID.randomUUID();
        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);

        CreateStageResponse createStageResponse = CreateStageResponse.from(stage);

        assertThat(createStageResponse.getUuid()).isEqualTo(stage.getUuid());

    }
}