package uk.gov.digital.ho.hocs.casework.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ReportMetadataDto {
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("columns")
    private List<ReportColumnDto> columns;
    @JsonProperty("id_column_key")
    private String idColumKey;
}
