package uk.gov.digital.ho.hocs.casework.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor()
@Getter
public class GetReportResponse<T extends ReportRow> {
    @JsonProperty("data")
    private List<T> data;
}
