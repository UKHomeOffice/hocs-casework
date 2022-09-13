package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseReferenceResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("reference")
    private String reference;

    public static GetCaseReferenceResponse from(UUID uuid, String reference) {
        return new GetCaseReferenceResponse(uuid, reference);
    }

}
