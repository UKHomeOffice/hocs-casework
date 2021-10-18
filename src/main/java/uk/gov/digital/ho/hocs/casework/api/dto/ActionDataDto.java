package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionDto.class, name = "EXTENSION"),
        @JsonSubTypes.Type(value = ActionDataAppealDto.class, name = "APPEAL"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActionDataDto {

    private UUID caseTypeActionUuid;
    private String caseTypeActionLabel;
}
