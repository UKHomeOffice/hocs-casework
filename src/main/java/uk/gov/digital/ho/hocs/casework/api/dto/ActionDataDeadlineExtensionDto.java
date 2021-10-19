package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("EXTENSION")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataDeadlineExtensionDto extends ActionDataDto {

    private String extendFrom;
    private int extendBy;
    private String note;

    public ActionDataDeadlineExtensionDto(UUID uuid, UUID caseTypeActionUuid, String caseTypeActionLabel, String extendFrom, int extendBy, String note) {
        super(uuid, caseTypeActionUuid, caseTypeActionLabel);
        this.extendFrom = extendFrom;
        this.extendBy = extendBy;
        this.note = note;
    }
}
