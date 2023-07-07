package uk.gov.digital.ho.hocs.casework.migration.api.exception;

import java.util.UUID;

public interface MigrationExceptions {
    class DuplicateMigratedReferenceException extends RuntimeException {
        private final String migratedReference;

        private final UUID existingCaseUUID;

        public DuplicateMigratedReferenceException(String migratedReference, UUID existingCaseUUID) {
            super("Existing case with migrated reference %s found, existing case UUID: %s".formatted(migratedReference, existingCaseUUID));
            this.existingCaseUUID = existingCaseUUID;
            this.migratedReference = migratedReference;
        }

        public String getMigratedReference() {
            return migratedReference;
        }

        public UUID getExistingCaseUUID() {
            return existingCaseUUID;
        }

    }

    class ValidStageNotFoundException extends RuntimeException {
        private final String migratedReference;
        private final String caseUUID;

        public ValidStageNotFoundException(String migratedReference, String caseUUID) {
            super("Could not find a stage when updating migrated reference %s found and case UUID: %s".formatted(migratedReference, caseUUID));
            this.migratedReference = migratedReference;
            this.caseUUID = caseUUID;
        }

        public String getMigratedReference() {
            return migratedReference;
        }

        public String getCaseUUID() {
            return caseUUID;
        }

    }
}
