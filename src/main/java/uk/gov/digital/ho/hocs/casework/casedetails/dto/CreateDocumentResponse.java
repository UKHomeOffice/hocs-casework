package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateDocumentResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    public static CreateDocumentResponse from(DocumentData documentData) {
        return new CreateDocumentResponse(documentData.getUuid());
    }
}
