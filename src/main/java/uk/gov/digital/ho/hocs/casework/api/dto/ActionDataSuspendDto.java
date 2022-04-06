package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("SUSPEND")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataSuspendDto extends ActionDataDto {

    private LocalDate dateOfSuspension;

    public ActionDataSuspendDto(UUID uuid, UUID caseTypeActionUuid, String caseSubtype, String caseTypeActionLabel,
                                LocalDate dateOfSuspension) {

        super(uuid, caseTypeActionUuid, caseSubtype, caseTypeActionLabel);
        this.dateOfSuspension = dateOfSuspension;
    }
}
