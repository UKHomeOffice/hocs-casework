package uk.gov.digital.ho.hocs.casework.client.documentclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;


@AllArgsConstructor()
@Getter
public class GetDocumentsResponse {

    @JsonProperty("documents")
    private Set<DocumentDto> documentDtos;

    @Setter
    @JsonProperty("documentTags")
    private List<String> documentTags;

    protected void replaceDocumentDtos(Set<DocumentDto> documentDtos) {
        this.documentDtos = documentDtos;
    }
}