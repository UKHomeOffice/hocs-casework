package uk.gov.digital.ho.hocs.casework.migration.api.exception;

import lombok.Getter;

import java.util.UUID;

public interface MigrationExceptions {
    @Getter
    class DuplicateMigratedReferenceException extends RuntimeException {
        private final String migratedReference;

        private final UUID existingCaseUUID;

        public DuplicateMigratedReferenceException(String migratedReference, UUID existingCaseUUID) {
            super("Existing case with migrated reference %s found, existing case UUID: %s".formatted(migratedReference, existingCaseUUID));
            this.existingCaseUUID = existingCaseUUID;
            this.migratedReference = migratedReference;
        }

    }

    @Getter
    class ValidStageNotFoundException extends RuntimeException {
        private final String migratedReference;
        private final String caseUUID;

        public ValidStageNotFoundException(String migratedReference, String caseUUID) {
            super(
                "Could not find a stage when updating migrated reference %s, case UUID: %s"
                    .formatted(migratedReference, caseUUID)
            );
            this.migratedReference = migratedReference;
            this.caseUUID = caseUUID;
        }

    }
}
