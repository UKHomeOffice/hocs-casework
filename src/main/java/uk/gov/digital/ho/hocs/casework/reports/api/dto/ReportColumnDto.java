package uk.gov.digital.ho.hocs.casework.reports.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
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
    @JsonProperty("filter_type")
    private FilterType filterType;

    public ReportColumnDto(
        @NonNull String key,
        @NonNull String displayName,
        @NonNull ColumnType type,
        @NonNull Boolean renderOnDashboard,
        @NonNull Boolean renderInCSV
                          ) {
        this.key = key;
        this.displayName = displayName;
        this.type = type;
        this.renderOnDashboard = renderOnDashboard;
        this.renderInCSV = renderInCSV;

        this.additionalFields = new HashMap<>();
    }

    public ReportColumnDto withAdditionalField(String key, String value) {
        additionalFields.put(key, value);
        return this;
    }

    public ReportColumnDto withFilter(FilterType filterType) {
        this.filterType = filterType;
        return this;
    }
}
