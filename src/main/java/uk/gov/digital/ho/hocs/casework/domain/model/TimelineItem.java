package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class TimelineItem {

    private UUID caseUUID;

    private UUID stageUUID;

    private LocalDateTime eventTime;

    private String userName;

    private String type;

    private String message;
}