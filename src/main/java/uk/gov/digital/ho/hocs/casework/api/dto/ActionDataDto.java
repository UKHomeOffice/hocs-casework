package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionInboundDto.class, name = "EXTENSION"),
        @JsonSubTypes.Type(value = ActionDataAppealDto.class, name = "APPEAL"),
        @JsonSubTypes.Type(value = ActionDataDeadlineExtensionOutboundDto.class, name = "EXTENSION_OUT"),
        @JsonSubTypes.Type(value = ActionDataExternalInterestInboundDto.class, name = "RECORD_INTEREST"),
        @JsonSubTypes.Type(value = ActionDataExternalInterestOutboundDto.class, name = "INTEREST_OUT"),
        @JsonSubTypes.Type(value = ActionDataSuspendDto.class, name = "SUSPEND")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActionDataDto {

    private UUID uuid;
    private UUID caseTypeActionUuid;
    private String caseSubtype;
    private String caseTypeActionLabel;
}
