package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageTagsDecoratorImplTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Stage stage;

    private static final String HOME_SEC_REPLY_FIELD_NAME = "HomeSecReply";
    private static final String PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "PrivateOfficeOverridePOTeamUUID";
    private static final String OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "OverridePOTeamUUID";
    private static final String PO_TEAM_UUID_FIELD_NAME = "POTeamUUID";
    private static final String HOME_SEC_PO_TEAM_UUID = "3d2c7893-92c5-4347-804a-8826f06f0c9d";
    private static final String OVERRIDE_PO_TEAM_NAME = "OverridePOTeamName";
    private static final String PO_TEAM_NAME = "POTeamName";

    private StageTagsDecoratorImpl stageTagsDecorator;

    @Before
    public void before() {
        stageTagsDecorator = new StageTagsDecoratorImpl(objectMapper);
    }

    @Test
    public void addsNoTagsWhenNotAHomeSecReplyStage() {
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(HOME_SEC_REPLY_FIELD_NAME, "FALSE"));

        ArrayList<String> tags = new ArrayList<>();

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }

    @Test
    public void addsTagWhenAHomeSecReplyStage() {
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(HOME_SEC_REPLY_FIELD_NAME, "TRUE"));
        when(stage.getStageType()).thenReturn("DCU_MIN_MARKUP");

        ArrayList<String> tags = new ArrayList<>();
        tags.add(StageTags.HOME_SEC_REPLY_TAG);

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }

    @Test
    public void addsTagWhenAHomeSecReplyNoPoTeamStage() {
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(
                HOME_SEC_REPLY_FIELD_NAME, "TRUE",
                OVERRIDE_PO_TEAM_UUID_FIELD_NAME, "",
                PO_TEAM_UUID_FIELD_NAME, ""
        ));
        when(stage.getStageType()).thenReturn("TEST_STAGE_TYPE");

        ArrayList<String> tags = new ArrayList<>();
        tags.add(StageTags.HOME_SEC_REPLY_TAG);

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }

    @Test
    public void addsTagWhenAHomeSecReplyPoTeamStage() {
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(
                HOME_SEC_REPLY_FIELD_NAME, "TRUE",
                PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME, HOME_SEC_PO_TEAM_UUID,
                OVERRIDE_PO_TEAM_UUID_FIELD_NAME, "",
                PO_TEAM_UUID_FIELD_NAME, ""
        ));
        when(stage.getStageType()).thenReturn("TEST_STAGE_TYPE");

        ArrayList<String> tags = new ArrayList<>();
        tags.add(StageTags.HOME_SEC_REPLY_TAG);

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }

    @Test
    public void addsTagsWhenOverrideToHSTeam(){
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(OVERRIDE_PO_TEAM_NAME, "Home Secretary"));
        when(stage.getStageType()).thenReturn("DCU_MIN_MARKUP");

        ArrayList<String> tags = new ArrayList<>();
        tags.add(StageTags.HOME_SEC_REPLY_TAG);

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }

    @Test
    public void addsTagsWhenDefaultToHSTeam(){
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(PO_TEAM_NAME, "Home Secretary"));
        when(stage.getStageType()).thenReturn("DCU_MIN_MARKUP");

        ArrayList<String> tags = new ArrayList<>();
        tags.add(StageTags.HOME_SEC_REPLY_TAG);

        stageTagsDecorator.decorateTags(stage);

        verify(stage).setTag(tags);
    }
}
