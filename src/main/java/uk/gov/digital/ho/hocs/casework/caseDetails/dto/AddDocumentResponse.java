package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AddDocumentResponse {

    private final UUID uuid;

    public static AddDocumentResponse from(DocumentData documentData) {
        return new AddDocumentResponse(documentData.getDocumentUUID());
    }
}
