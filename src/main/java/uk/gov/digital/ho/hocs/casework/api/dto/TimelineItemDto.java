package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TimelineItemDto {

    private UUID caseUUID;

    private UUID stageUUID;

    private LocalDateTime eventTime;

    private String userName;

    private String type;

    @JsonRawValue
    private String body;

    public static TimelineItemDto from(TimelineItem timelineItem) {
        return new TimelineItemDto(timelineItem.getCaseUUID(), timelineItem.getStageUUID(), timelineItem.getEventTime(), timelineItem.getUserName(), timelineItem.getType(), timelineItem.getMessage());
    }
}