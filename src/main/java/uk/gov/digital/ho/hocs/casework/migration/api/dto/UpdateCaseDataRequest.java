package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
public class UpdateCaseDataRequest {
    private LocalDateTime updateEventTimestamp;

    private Map<String, String> data;
}
