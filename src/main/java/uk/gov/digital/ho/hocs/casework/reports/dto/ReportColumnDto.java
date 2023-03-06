package uk.gov.digital.ho.hocs.casework.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class ReportColumnDto {
    @JsonProperty("key")
    @NonNull
    private String key;
    @JsonProperty("display_name")
    @NonNull
    private String displayName;
    @JsonProperty("type")
    @NonNull
    private ColumnType type;
    @JsonProperty("render_on_dashboard")
    @NonNull
    private Boolean renderOnDashboard;
    @JsonProperty("render_in_csv")
    @NonNull
    private Boolean renderInCSV;
    @JsonProperty("additional_fields")
    private Map<String, String> additionalFields;
}
