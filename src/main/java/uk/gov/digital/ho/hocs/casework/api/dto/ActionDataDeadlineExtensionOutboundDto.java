package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("EXTENSION_OUT")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataDeadlineExtensionOutboundDto extends ActionDataDto {

    private LocalDate originalDeadline;
    private LocalDate updatedDeadline;
    private String note;

    public ActionDataDeadlineExtensionOutboundDto(UUID uuid, UUID caseTypeActionUuid, String caseTypeActionLabel, LocalDate originalDeadline, LocalDate updatedDeadline, String note) {
        super(uuid, caseTypeActionUuid, caseTypeActionLabel);
        this.originalDeadline = originalDeadline;
        this.updatedDeadline = updatedDeadline;
        this.note = note;
    }
}
