package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicTest {

    @Test
    public void getTopic() {

        UUID caseUUID = UUID.randomUUID();
        String text = "anyTitle";
        UUID textUUID = UUID.randomUUID();

        Topic topic = new Topic(caseUUID, text, textUUID);

        assertThat(topic.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(topic.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(topic.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(topic.getText()).isEqualTo(text);
        assertThat(topic.getTextUUID()).isEqualTo(textUUID);
        assertThat(topic.isDeleted()).isFalse();

    }

    @Test
    public void getTopicDeleted() {

        UUID caseUUID = UUID.randomUUID();
        String text = "anyTitle";
        UUID textUUID = UUID.randomUUID();

        Topic topic = new Topic(caseUUID, text, textUUID);

        assertThat(topic.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(topic.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(topic.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(topic.getText()).isEqualTo(text);
        assertThat(topic.getTextUUID()).isEqualTo(textUUID);
        assertThat(topic.isDeleted()).isFalse();

        topic.setDeleted(true);

        assertThat(topic.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(topic.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(topic.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(topic.getText()).isEqualTo(text);
        assertThat(topic.getTextUUID()).isEqualTo(textUUID);
        assertThat(topic.isDeleted()).isTrue();

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getTopicNullCaseUUID() {

        String text = "anyTitle";
        UUID textUUID = UUID.randomUUID();

        new Topic(null, text, textUUID);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getTopicNullType() {

        UUID caseUUID = UUID.randomUUID();
        UUID textUUID = UUID.randomUUID();

        new Topic(caseUUID, null, textUUID);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getTopicNullText() {

        UUID caseUUID = UUID.randomUUID();
        String text = "anyTitle";

        new Topic(caseUUID, text, null);
    }

}