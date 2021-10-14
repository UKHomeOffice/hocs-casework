package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionDto.class, name = "EXTENSION"),
        @JsonSubTypes.Type(value = ActionDataAppealDto.class, name = "APPEAL"),
})
public abstract class ActionDataDto {}
