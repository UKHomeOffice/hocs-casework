package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_CREATE_FAILURE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "case_data")
public class CaseData extends AbstractJsonDataMap implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid", columnDefinition ="uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Setter
    @Getter
    @Column(name = "deleted")
    private boolean deleted;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    @Column(name = "data")
    private String data = "{}";

    @Setter
    @Getter
    @Column(name = "primary_topic_uuid")
    private UUID primaryTopicUUID;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_topic_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Topic primaryTopic;

    @Setter
    @Getter
    @Column(name = "primary_correspondent_uuid")
    private UUID primaryCorrespondentUUID;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_correspondent_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Correspondent primaryCorrespondent;

    @Setter
    @Getter
    @Column(name = "case_deadline")
    private LocalDate caseDeadline;

    @Setter
    @Getter
    @Column(name = "case_deadline_warning")
    private LocalDate caseDeadlineWarning;

    @Setter
    @Getter
    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Setter
    @Getter
    @Column(name = "completed")
    private boolean completed;

    @Getter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false)
    private Set<ActiveStage> activeStages;

    @Getter
    @Setter
    @OneToMany(
            mappedBy = "caseData",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<CaseDeadlineExtension> deadlineExtensions;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    @Where(clause = "deleted = false")
    private Set<CaseNote> caseNotes;

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, ObjectMapper objectMapper, LocalDate dateReceived) {
        this(type, caseNumber, dateReceived);
        update(data, objectMapper);
    }

    public void addDeadlineExtension(CaseDeadlineExtensionType caseDeadlineExtensionType) {
        CaseDeadlineExtension caseDeadlineExtension = new CaseDeadlineExtension(this, caseDeadlineExtensionType);
        deadlineExtensions.add(caseDeadlineExtension);
    }

    public CaseData(CaseDataType type, Long caseNumber, LocalDate dateReceived) {
        if (type == null || caseNumber == null) {
            throw new ApplicationExceptions.EntityCreationException("Cannot create CaseData", CASE_CREATE_FAILURE);
        }

        this.type = type.getDisplayCode();
        this.reference = CaseReferenceGenerator.generateCaseReference(this.type, caseNumber, this.created);
        this.uuid = randomUUID(type.getShortCode());
        this.dateReceived = dateReceived;
    }

    private static UUID randomUUID(String shortCode) {
        if (shortCode != null) {
            String uuid = UUID.randomUUID().toString().substring(0, 33);
            return UUID.fromString(uuid.concat(shortCode));
        } else {
            throw new ApplicationExceptions.EntityCreationException("shortCode is null", CASE_CREATE_FAILURE);
        }
    }


    // --------  Migration Code Start --------
    public CaseData(CaseDataType type, String caseReference, Map<String, String> data, ObjectMapper objectMapper, LocalDate caseDeadline, LocalDate dateReceived, LocalDateTime caseCreated) {
        this(type, caseReference, caseDeadline, dateReceived, caseCreated);
        update(data, objectMapper);
    }

    public CaseData(CaseDataType type, String caseReference, LocalDate caseDeadline, LocalDate dateReceived, LocalDateTime caseCreated) {
        if (type == null || caseReference == null) {
            throw new ApplicationExceptions.EntityCreationException("Cannot create CaseData", CASE_CREATE_FAILURE);
        }
        this.created = caseCreated;
        this.type = type.getDisplayCode();
        this.reference = caseReference;
        this.uuid = randomUUID(type.getShortCode());
        this.caseDeadline = caseDeadline;
        this.dateReceived = dateReceived;
    }


    // --------  Migration Code End --------

}
