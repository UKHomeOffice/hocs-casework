package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DashboardResponse {

    @JsonProperty("stages")
    private final List<DashboardStatisticDto> dashboardStatistics;

    public static DashboardResponse from(Map<UUID, Map<String, Integer>> dashboardStageStatistics) {
        return new DashboardResponse(dashboardStageStatistics.entrySet().stream()
                .map((stageStatistic) -> new DashboardStatisticDto(stageStatistic.getKey(), stageStatistic.getValue()))
                .collect(Collectors.toList()));
    }

    @AllArgsConstructor
    @Getter
    public static class DashboardStatisticDto {
        private final UUID teamUuid;
        private final Map<String, Integer> statistics;
    }

}
