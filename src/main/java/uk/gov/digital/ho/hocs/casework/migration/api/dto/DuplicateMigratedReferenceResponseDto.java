package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.migration.api.exception.MigrationExceptions;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class DuplicateMigratedReferenceResponseDto {
    @JsonProperty("existing_case_id")
    private UUID existingCaseId;
    @JsonProperty("migrated_reference")
    private String migratedReference;

    public static DuplicateMigratedReferenceResponseDto fromException(MigrationExceptions.DuplicateMigratedReferenceException e) {
        return new DuplicateMigratedReferenceResponseDto(
            e.getExistingCaseUUID(),
            e.getMigratedReference()
        );
    }

}
