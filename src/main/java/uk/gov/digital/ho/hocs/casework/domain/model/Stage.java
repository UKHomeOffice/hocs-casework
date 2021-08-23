package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@NoArgsConstructor
@Entity
@Table(name = "stage")
public class Stage extends AbstractJsonDataMap implements Serializable {

    public static final String DCU_MIN_INITIAL_DRAFT = "DCU_MIN_INITIAL_DRAFT";
    public static final String DCU_TRO_INITIAL_DRAFT = "DCU_TRO_INITIAL_DRAFT";
    public static final String DCU_DTEN_INITIAL_DRAFT = "DCU_DTEN_INITIAL_DRAFT";
    public static final String OFFLINE_QA_USER = "OfflineQaUser";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String stageType;

    @Getter
    @Column(name = "deadline")
    private LocalDate deadline;

    @Getter
    @Column(name = "deadline_warning")
    private LocalDate deadlineWarning;

    @Getter
    @Column(name = "transition_note_uuid", columnDefinition = "uuid")
    private UUID transitionNoteUUID;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_note_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private CaseNote transitionNote;

    @Getter
    @Column(name = "case_uuid", columnDefinition = "uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "team_uuid", columnDefinition = "uuid")
    private UUID teamUUID;

    @Getter
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUUID;

    @Getter
    @Column(name = "case_reference", insertable = false, updatable = false)
    private String caseReference;

    @Getter
    @Column(name = "case_type", insertable = false, updatable = false)
    private String caseDataType;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    @Column(name = "data", insertable = false, updatable = false)
    private String data;

    @Getter
    @Column(name = "case_created", insertable = false, updatable = false)
    private LocalDateTime caseCreated;

    @Getter
    @Column(name = "correspondents", insertable = false, updatable = false)
    private String correspondents;

    @Getter
    @Column(name = "case_assigned_topic", insertable = false, updatable = false)
    private String assignedTopic;

    @Getter
    @Setter
    @Basic(optional = true)
    @Column(name = "somu", insertable = false, updatable = false)
    private String somu;

    @Getter
    @Setter
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArrayList<String> tag;

    @Getter
    @Setter
    @Transient
    private String dueContribution;

    @Getter
    @Setter
    @Transient
    private String contributions;

    @Getter
    @Setter
    @Transient
    @JsonInclude
    private String nextCaseType;

    @Getter
    @JsonInclude
    @Column(name = "secondary_case_reference", insertable = false, updatable = false)
    private String nextCaseReference;

    @Getter
    @JsonInclude
    @Column(name = "secondary_case_uuid", insertable = false, updatable = false)
    private String nextCaseUUID;

    public Stage(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, UUID transitionNoteUUID) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        this.transitionNoteUUID = transitionNoteUUID;
        setTeam(teamUUID);
        setUser(userUUID);
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setDeadlineWarning(LocalDate deadlineWarning){
        this.deadlineWarning = deadlineWarning;
    }

    public void setTransitionNote(UUID transitionNoteUUID) {
        this.transitionNoteUUID = transitionNoteUUID;
    }

    public void setTeam(UUID teamUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = null;
    }

    public void setUser(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public void setAssignedTopic(String assignedTopic) {
        this.assignedTopic = assignedTopic;
    }

    public boolean isActive() {
        return this.teamUUID != null;
    }

}
