package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetStagesResponseTest {

    @Test
    public void getGetStagesResponseResponse() {

        UUID caseUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UUID transitionNoteUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        Set<Stage> stages = new HashSet<>();
        stages.add(stage);

        GetStagesResponse getStagesResponse = GetStagesResponse.from(stages);

        assertThat(getStagesResponse.getStages()).hasSize(1);

    }

    @Test
    public void getGetStagesResponseEmpty() {

        Set<Stage> stages = new HashSet<>();

        GetStagesResponse getStagesResponse = GetStagesResponse.from(stages);

        assertThat(getStagesResponse.getStages()).hasSize(0);

    }

}