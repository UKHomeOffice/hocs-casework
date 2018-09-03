package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetDocumentsResponse {

    @JsonProperty("documents")
    private Set<GetDocumentResponse> documents;

    public static GetDocumentsResponse from(Set<DocumentData> documents) {
        Set<GetDocumentResponse> documentResponses = documents
                .stream()
                .map(GetDocumentResponse::from)
                .collect(Collectors.toSet());

        return new GetDocumentsResponse(documentResponses);
    }
}