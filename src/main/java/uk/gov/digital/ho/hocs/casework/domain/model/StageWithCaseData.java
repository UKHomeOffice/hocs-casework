package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.util.JsonDataMapUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@NoArgsConstructor
@Entity
@Table(name = "stage")
public class StageWithCaseData extends BaseStage {

    public interface StageTeamUuid {
        String getTeamUuid();
    }

    public static final String DCU_MIN_INITIAL_DRAFT = "DCU_MIN_INITIAL_DRAFT";
    public static final String DCU_TRO_INITIAL_DRAFT = "DCU_TRO_INITIAL_DRAFT";
    public static final String DCU_DTEN_INITIAL_DRAFT = "DCU_DTEN_INITIAL_DRAFT";
    public static final String OFFLINE_QA_USER = "OfflineQaUser";

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_note_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private CaseNote transitionNote;


    @Getter
    @Column(name = "case_reference", insertable = false, updatable = false)
    private String caseReference;

    @Setter
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

    @Getter
    @JsonInclude
    @Column(name = "secondary_stage_uuid", insertable = false, updatable = false)
    private String nextCaseStageUUID;

    @Setter
    @Getter
    @JsonInclude
    @Column(name = "completed", insertable = false, updatable = false)
    private Boolean completed;

    public StageWithCaseData(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, UUID transitionNoteUUID) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        this.transitionNoteUUID = transitionNoteUUID;
        completed = Boolean.FALSE;
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

    public void update(Map<String, String> newData, ObjectMapper objectMapper) {
        setData(JsonDataMapUtils.update(getData(), newData, objectMapper));
    }

    public Map<String, String> getDataMap(ObjectMapper objectMapper) {
        return JsonDataMapUtils.getDataMap(getData(), objectMapper);
    }

}
