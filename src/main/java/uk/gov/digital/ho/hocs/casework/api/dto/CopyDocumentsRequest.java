package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CopyDocumentsRequest {

    private UUID fromReferenceUUID;

    private UUID toReferenceUUID;

    private Set<String> types;

}
