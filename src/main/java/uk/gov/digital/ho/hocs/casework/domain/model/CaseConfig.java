package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CaseConfig {
    @Getter
    private final String type;

    @Getter
    private final List<CaseTab> tabs;
}