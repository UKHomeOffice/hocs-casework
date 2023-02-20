package uk.gov.digital.ho.hocs.casework.reports.domain.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.reports.dto.ReportRow;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "report_open_cases")
public class OpenCasesRow implements ReportRow, Serializable {
    @Id
    @JsonProperty("case_uuid")
    private UUID caseUUID;

    @JsonProperty("business_area")
    private String businessArea;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("case_deadline")
    private LocalDate caseDeadline;

    @JsonProperty("stage_type")
    private String stageType;

    @JsonProperty("user_group")
    private String userGroup;

    @JsonProperty("outside_service_standard")
    private Boolean outsideServiceStandard;

}
