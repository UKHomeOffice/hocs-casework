package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class DocumentResponse {

    private final UUID uuid;

    public static DocumentResponse from(DocumentData documentData) {
        return new DocumentResponse(documentData.getDocumentUUID());
    }
}
