package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rsh_case_details")
@Getter
@Setter
@Builder
public class RshCaseDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    @JsonProperty(value = "id")
    private int id;

    @Column(name = "ref")
    private Long ref;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "case_reference")
    private String caseReference;

    @Column(name = "leg_ref")
    private String legRef;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "forename")
    private String forename;

    @Column(name = "surname")
    private String surname;

    @Column(name = "case_type")
    @JsonProperty(value = "caseType", required = true)
    private String caseType;

    @Column(name = "case_created")
    private LocalDateTime caseCreated;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "case_data")
    @JsonProperty(value = "caseData", required = true)
    private String caseData;
}
