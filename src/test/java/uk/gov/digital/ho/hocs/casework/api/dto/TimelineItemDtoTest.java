package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class TimelineItemDtoTest {

    @Test
    public void getTimelineDtoTest() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        LocalDateTime eventTime = LocalDateTime.of(2019,12,5,14,5,1,222);
        String userName = "user";
        String type = "CASE_NOTE";
        String body = "{some: data}";

        TimelineItem timeline = new TimelineItem(caseUUID,stageUUID,eventTime,userName,type, body);

        TimelineItemDto timelineDto = TimelineItemDto.from(timeline);

        assertThat(timelineDto.getCaseUUID()).isEqualTo(timeline.getCaseUUID());
        assertThat(timelineDto.getStageUUID()).isEqualTo(timeline.getStageUUID());
        assertThat(timelineDto.getEventTime().toLocalDateTime()).isEqualTo(timeline.getEventTime());
        assertThat(timelineDto.getEventTime().getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(timelineDto.getUserName()).isEqualTo(timeline.getUserName());
        assertThat(timelineDto.getBody()).isEqualTo(timeline.getMessage());

    }

}