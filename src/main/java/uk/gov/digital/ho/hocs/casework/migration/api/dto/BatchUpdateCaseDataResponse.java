package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class BatchUpdateCaseDataResponse
{
    @JsonProperty("migrated_reference")
    private String migratedReference;
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("error_message")
    private String errorMessage;

    public static BatchUpdateCaseDataResponse success(String migratedReference) {
        return new BatchUpdateCaseDataResponse(migratedReference, true, null);
    }
    public static BatchUpdateCaseDataResponse error(String migratedReference, String errorMessage) {
        return new BatchUpdateCaseDataResponse(migratedReference, false, errorMessage);
    }
}
