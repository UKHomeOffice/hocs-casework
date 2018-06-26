package uk.gov.digital.ho.hocs.casework.caseDetails.model;

public enum DocumentStatus {

    PENDING,
    UPLOADED,
    FAILED_VIRUS_SCAN,
    FAILED_PDF_GENERATION,
    FAILED_VALIDATION_FILE_EXTENSION,
    FAILED_VALIDATION_FILE_MAX_SIZE,
    FALIED_VALIDATION_FILE_TYPE,
    FAILED
    
}
