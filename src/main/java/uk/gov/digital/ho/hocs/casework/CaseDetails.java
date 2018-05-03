package uk.gov.digital.ho.hocs.casework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "case_details")
@Getter
public class CaseDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_generator")
    @SequenceGenerator(name = "ref_generator", sequenceName = "ref_seq", allocationSize = 1)
    @Column(name = "ref", nullable = false, unique = true)
    @JsonProperty(value = "ref")
    private int ref;

//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

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
    @JsonProperty(value = "caseCreated", required = true)
    private LocalDateTime caseCreated;

    @Column(name = "case_data")
    @JsonProperty(value = "caseData", required = true)
    private String caseData;
}
