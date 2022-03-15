package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CopyDocumentsRequest {

    private UUID fromReferenceUUID;

    private UUID toReferenceUUID;

    private Set<String> types;

}