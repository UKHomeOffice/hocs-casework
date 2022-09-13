package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TimelineItemDto {

    private UUID caseUUID;

    private UUID stageUUID;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z", timezone = "UTC")
    private ZonedDateTime eventTime;

    private String userName;

    private String type;

    @JsonRawValue
    private String body;

    private UUID timelineItemUUID;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z", timezone = "UTC")
    private ZonedDateTime editedTime;

    private String editorName;

    public static TimelineItemDto from(TimelineItem timelineItem) {
        return new TimelineItemDto(timelineItem.getCaseUUID(), timelineItem.getStageUUID(),
            ZonedDateTime.of(timelineItem.getEventTime(), ZoneOffset.UTC), timelineItem.getUserName(),
            timelineItem.getType(), timelineItem.getMessage(), timelineItem.getTimelineItemUUID(),
            timelineItem.getEditedTime()!=null ? ZonedDateTime.of(timelineItem.getEditedTime(), ZoneOffset.UTC):null,
            timelineItem.getEditorName());
    }

}