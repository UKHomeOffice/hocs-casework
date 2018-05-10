package uk.gov.digital.ho.hocs.casework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "case_details")
@Getter
@Setter
@Builder
public class CaseDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    @JsonProperty(value = "id")
    private int id;

    @Column(name = "ref")
    private Long ref;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "case_type")
    @JsonProperty(value = "caseType", required = true)
    private String caseType;

    @Column(name = "workflow_version")
    @JsonProperty(value = "workflowVersion")
    private String workflowVersion;

    @Column(name = "stage")
    @JsonProperty(value = "stage", required = true)
    private String stage;

    @Column(name = "allocated_team")
    @JsonProperty(value = "allocatedTeam")
    private String allocatedTeam;

    @Column(name = "allocated_user")
    @JsonProperty(value = "allocatedUser")
    private String allocatedUser;

    @Column(name = "case_created")
    private LocalDateTime caseCreated;

    @Column(name = "case_data")
    @JsonProperty(value = "caseData", required = true)
    private String caseData;
}
