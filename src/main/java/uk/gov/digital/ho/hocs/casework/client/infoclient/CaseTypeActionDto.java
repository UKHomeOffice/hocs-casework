package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class CaseTypeActionDto {

    private UUID uuid;
    private UUID caseTypeUuid;
    private String caseType;
    private String actionType;
    private String actionLabel;
    private int sortOrder;
    private boolean active;
    private String props;
}
