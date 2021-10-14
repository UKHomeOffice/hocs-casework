package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@JsonTypeName("EXTENSION")
@ToString
public class ActionDataDeadlineExtensionDto extends ActionDataDto {

    private UUID caseTypeActionUuid;
    private String caseTypeActionLabel;
    private String extendFrom;
    private int extendBy;
    private String note;

}
