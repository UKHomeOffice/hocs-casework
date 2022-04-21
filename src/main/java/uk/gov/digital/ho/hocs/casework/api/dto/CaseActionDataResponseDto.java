package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CaseActionDataResponseDto {

    private Map<String, List<ActionDataDto>> caseActionData;
    private List<CaseTypeActionDto> caseTypeActionData;
    private LocalDate currentCaseDeadline;
    private int remainingDaysUntilDeadline;

    public static CaseActionDataResponseDto from(Map<String, List<ActionDataDto>> caseActionData, List<CaseTypeActionDto> caseTypeActionData, LocalDate currentCaseDeadline, int remainingDays) {

        Map<String, List<ActionDataDto>> caseActionDataMap = new HashMap<>(caseActionData);
        List<CaseTypeActionDto> caseTypeActionDataList = new ArrayList<>(caseTypeActionData);

        return new CaseActionDataResponseDto(caseActionDataMap, caseTypeActionDataList, currentCaseDeadline, remainingDays);
    }
}
