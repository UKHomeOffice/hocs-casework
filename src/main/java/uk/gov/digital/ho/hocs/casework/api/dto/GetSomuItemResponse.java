package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"uuid"})
@Getter
public class GetSomuItemResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("case_uuid")
    private UUID caseUuid;

    @JsonProperty("somu_uuid")
    private UUID somuUuid;

    @JsonProperty("data")
    private String data;
    
    @JsonProperty("deleted")
    private boolean deleted;
    
    public static GetSomuItemResponse from(SomuItem somuItem) {
        return new GetSomuItemResponse(somuItem.getUuid(), 
                somuItem.getCaseUuid(), 
                somuItem.getSomuUuid(), 
                somuItem.getData(),
                somuItem.isDeleted());
    }
    
}
