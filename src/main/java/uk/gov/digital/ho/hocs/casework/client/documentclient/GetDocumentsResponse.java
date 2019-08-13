package uk.gov.digital.ho.hocs.casework.client.documentclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;


@AllArgsConstructor()
@Getter
public class GetDocumentsResponse {

    @JsonProperty("documents")
    private Set<DocumentDto> documentDtos;

}