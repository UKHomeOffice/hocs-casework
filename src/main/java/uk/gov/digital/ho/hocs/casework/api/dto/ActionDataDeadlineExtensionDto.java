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
@ToString
@JsonTypeName("EXTENSION")
public class ActionDataDeadlineExtensionDto extends ActionDataDto {

    private String extendFrom;
    private int extendBy;
    private String note;

    public ActionDataDeadlineExtensionDto(UUID caseTypeActionUuid, String caseTypeActionLabel, String extendFrom, int extendBy, String note) {
        super(caseTypeActionUuid, caseTypeActionLabel);
        this.extendFrom = extendFrom;
        this.extendBy = extendBy;
        this.note = note;
    }
}
