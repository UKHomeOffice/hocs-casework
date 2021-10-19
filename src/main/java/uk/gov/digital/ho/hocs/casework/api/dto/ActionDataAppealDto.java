package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("APPEAL")
public class ActionDataAppealDto extends ActionDataDto {

    private String data;

    public ActionDataAppealDto(UUID uuid, UUID caseTypeActionUuid, String caseTypeActionLabel, String data) {
        super(uuid, caseTypeActionUuid, caseTypeActionLabel);
        this.data = data;
    }
}
