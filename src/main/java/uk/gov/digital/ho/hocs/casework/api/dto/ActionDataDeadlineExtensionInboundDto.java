package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("EXTENSION")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataDeadlineExtensionInboundDto extends ActionDataDto {

    private String extendFrom;

    private int extendBy;

    private String note;

    private String reasons;

    public ActionDataDeadlineExtensionInboundDto(UUID uuid,
                                                 UUID caseTypeActionUuid,
                                                 String caseSubtype,
                                                 String caseTypeActionLabel,
                                                 String extendFrom,
                                                 int extendBy,
                                                 String note,
                                                 String reasons) {
        super(uuid, caseTypeActionUuid, caseSubtype, caseTypeActionLabel);
        this.extendFrom = extendFrom;
        this.extendBy = extendBy;
        this.note = note;
        this.reasons = reasons;
    }

}
