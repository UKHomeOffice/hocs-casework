package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Converts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATE_FAILURE;

@Converts({ @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)})
@Entity
@Table(name = "stage")
@SuppressWarnings("JpaAttributeTypeInspection")
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
    //@Type(type = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
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
    private Set<CaseDataTag> tag = new HashSet<>();

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
    @Column(name = "date_completed", insertable = false, updatable = false)
    private LocalDateTime dateCompleted;

    public StageWithCaseData(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, UUID transitionNoteUUID) {
        if (caseUUID == null || stageType == null) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot create Stage (%s, %s).", caseUUID, stageType), STAGE_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.stageType = stageType;
        this.transitionNoteUUID = transitionNoteUUID;
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
        this.data = new HashMap<>();
        this.tag = new HashSet<>();
    }

    public StageWithCaseData() {
        this.data = new HashMap<>();
    }

    public void putData(String key, String value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }

        this.data.put(key, value);
    }

    public String getData(String key) {
        return this.data.getOrDefault(key, null);
    }

    public void appendTags(List<String> tags) {
        if (tag == null) {
            tag = new HashSet<>();
        }

        tag.addAll(tags.stream().map(tagStr -> new CaseDataTag(this.caseUUID, tagStr)).toList());
    }

}
