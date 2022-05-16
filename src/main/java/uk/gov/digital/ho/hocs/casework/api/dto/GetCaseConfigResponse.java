package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseConfig;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseTab;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GetCaseConfigResponse {
    String type;

    List<CaseTab> tabs;

    public static GetCaseConfigResponse from(CaseConfig caseConfig) {
        return new GetCaseConfigResponse(caseConfig.getType(), caseConfig.getTabs());
    }
}