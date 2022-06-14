package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@NoArgsConstructor
@AllArgsConstructor
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Entity
@Table(name = "stage")
public class StageWithCaseData extends BaseStage {

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
    @Setter(value = AccessLevel.PROTECTED)
    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "jsonb", insertable = false, updatable = false)
    private Map<String, String> data;


    @Getter
    @Column(name = "case_created", insertable = false, updatable = false)
    private LocalDateTime caseCreated;

    @Getter
    @Column(name = "correspondents", insertable = false, updatable = false)
    private String correspondents;

    @Getter
    @Setter
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
        this.completed = Boolean.FALSE;
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
    }

    public StageWithCaseData(UUID uuid, UUID transitionNoteUUID, String caseReference, String caseDataType, LocalDateTime caseCreated, String correspondents, String assignedTopic, String somu, ArrayList<String> tag, String dueContribution, String contributions, String nextCaseType, String nextCaseReference, String nextCaseUUID, boolean completed) {
        this.uuid = uuid;
        this.transitionNoteUUID = transitionNoteUUID;
        this.caseReference = caseReference;
        this.caseDataType = caseDataType;
        this.caseCreated = caseCreated;
        this.correspondents = correspondents;
        this.assignedTopic = assignedTopic;
        this.somu = somu;
        this.tag = tag;
        this.dueContribution = dueContribution;
        this.contributions = contributions;
        this.nextCaseType = nextCaseType;
        this.nextCaseReference = nextCaseReference;
        this.nextCaseUUID = nextCaseUUID;
        this.completed = completed;
    }

    public void putData(String key, String value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }

        this.data.put(key, value);
    }

    public void putAllData(Map<String,String> data) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }

        this.data.putAll(data);
    }

    public String getData(String key) {
        return this.data.getOrDefault(key, null);
    }

}
