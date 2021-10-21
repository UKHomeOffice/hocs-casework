package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionInboundDto.class, name = "EXTENSION"),
        @JsonSubTypes.Type(value = ActionDataAppealDto.class, name = "APPEAL"),
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionOutboundDto.class, name = "EXTENSION_OUT")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActionDataDto {

    private UUID uuid;
    private UUID caseTypeActionUuid;
    private String caseTypeActionLabel;
}
