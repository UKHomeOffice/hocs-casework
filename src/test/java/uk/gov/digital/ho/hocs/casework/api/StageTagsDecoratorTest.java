package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StageTagsDecoratorTest {

    private static final String HOME_SEC_REPLY_FIELD_NAME = "HomeSecReply";

    private StageTagsDecorator stageTagsDecorator;

    @Before
    public void before() {
        stageTagsDecorator = new StageTagsDecorator();
    }

    @Test
    public void addsTagWhenHomeSecReplyTrue() {
        var tags = stageTagsDecorator.decorateTags(Map.of(HOME_SEC_REPLY_FIELD_NAME, "TRUE"));
        assertTrue(tags.contains(StageTags.HOME_SEC_REPLY_TAG));
    }

    @Test
    public void addsNoTagsWhenHomeSecReplyFalse() {
        var tags = stageTagsDecorator.decorateTags(Map.of(HOME_SEC_REPLY_FIELD_NAME, "FALSE"));
        assertEquals(0, tags.size());
    }

    @Test
    public void addsNoTagsWhenNoHomeSecReplyValue() {
        var tags = stageTagsDecorator.decorateTags(Map.of());
        assertEquals(0, tags.size());
    }

}
