package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UpdateStageTransitionNoteRequest {

    @JsonProperty("transitionNoteUUID")
    private UUID transitionNoteUUID;

}
