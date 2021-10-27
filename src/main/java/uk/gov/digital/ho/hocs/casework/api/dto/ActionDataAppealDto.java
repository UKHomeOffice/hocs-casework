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
@JsonTypeName("APPEAL")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataAppealDto extends ActionDataDto {

    private String status;
    private LocalDate dateSentRMS;
    private String outcome;
    private String complexCase;
    private String note;
    private String appealOfficerData;

    public ActionDataAppealDto(UUID uuid, UUID caseTypeActionUuid, String caseTypeActionLabel,
                               String status,
                               LocalDate dateSentRMS,
                               String outcome,
                               String complexCase,
                               String note,
                               String appealOfficerData) {

        super(uuid, caseTypeActionUuid, caseTypeActionLabel);
        this.status = status;
        this.dateSentRMS = dateSentRMS;
        this.outcome = outcome;
        this.complexCase = complexCase;
        this.note = note;
        this.appealOfficerData = appealOfficerData;

    }
}
