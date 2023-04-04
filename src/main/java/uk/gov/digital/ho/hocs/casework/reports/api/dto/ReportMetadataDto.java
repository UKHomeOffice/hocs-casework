package uk.gov.digital.ho.hocs.casework.reports.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;

import java.util.List;

@AllArgsConstructor
@Getter
public class ReportMetadataDto {
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("supported_case_types")
    private List<CaseType> supportedCaseTypes;
    @JsonProperty("columns")
    private List<ReportColumnDto> columns;
    @JsonProperty("id_column_key")
    private String idColumKey;
}
