package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StageTagsDecoratorImplTest {

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
        stageTagsDecorator = new StageTagsDecoratorImpl();
    }

    @Test
    public void addsNoTagsWhenNotAHomeSecReplyStage() {
        var tags = stageTagsDecorator.decorateTags(Map.of(HOME_SEC_REPLY_FIELD_NAME, "FALSE"), "AnyType");
        assertEquals(0, tags.size());
    }

    @Test
    public void addsTagWhenAHomeSecReplyStage() {
        var tags = stageTagsDecorator.decorateTags(Map.of(HOME_SEC_REPLY_FIELD_NAME, "TRUE"), "DCU_MIN_MARKUP");
        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

    @Test
    public void addsTagWhenAHomeSecReplyNoPoTeamStage() {
        var tags = stageTagsDecorator.decorateTags(
            Map.of(HOME_SEC_REPLY_FIELD_NAME, "TRUE", OVERRIDE_PO_TEAM_UUID_FIELD_NAME, "", PO_TEAM_UUID_FIELD_NAME,
                ""), "TEST_STAGE_TYPE");

        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

    @Test
    public void addsTagWhenAHomeSecReplyPoTeamStage() {
        var tags = stageTagsDecorator.decorateTags(
            Map.of(HOME_SEC_REPLY_FIELD_NAME, "TRUE", PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME,
                HOME_SEC_PO_TEAM_UUID, OVERRIDE_PO_TEAM_UUID_FIELD_NAME, "", PO_TEAM_UUID_FIELD_NAME, ""),
            "TEST_STAGE_TYPE");

        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

    @Test
    public void addsTagsWhenOverrideToHSTeam() {
        var tags = stageTagsDecorator.decorateTags(Map.of(OVERRIDE_PO_TEAM_NAME, "Home Secretary"), "DCU_MIN_MARKUP");

        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

    @Test
    public void addsTagsWhenDefaultToHSTeam() {
        var tags = stageTagsDecorator.decorateTags(Map.of(PO_TEAM_NAME, "Home Secretary"), "DCU_MIN_MARKUP");

        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

}
