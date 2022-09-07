package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DashboardResponse {

    @JsonProperty("stages")
    private final List<DashboardSummaryDto> dashboardSummaries;

    public static DashboardResponse from(Map<UUID, Map<String, Integer>> dashboardStageSummary) {
        return new DashboardResponse(dashboardStageSummary.entrySet().stream().map(
            (stageSummary) -> new DashboardSummaryDto(stageSummary.getKey(), stageSummary.getValue())).collect(
            Collectors.toList()));
    }

    @AllArgsConstructor
    @Getter
    public static class DashboardSummaryDto {

        private final UUID teamUuid;

        private final Map<String, Integer> statistics;

    }

}
