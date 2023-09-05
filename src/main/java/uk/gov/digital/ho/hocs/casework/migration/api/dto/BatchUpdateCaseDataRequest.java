package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BatchUpdateCaseDataRequest {

    private String migratedReference;

    private LocalDateTime updateEventTimestamp;

    private Map<String, String> data;
}
