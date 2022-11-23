package uk.gov.digital.ho.hocs.casework.domain.model.workstacks;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "WorkstackCaseData")
@Table(name = "case_data")
public class CaseData implements Serializable {

    @Id
    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
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
    @Setter(value = AccessLevel.PROTECTED)
    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, String> dataMap = new HashMap<>(0);

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
    @JoinColumn(name = "primary_correspondent_uuid",
                referencedColumnName = "uuid",
                insertable = false,
                updatable = false)
    private Correspondent primaryCorrespondent;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    @Where(clause = "deleted = false")
    private Set<Correspondent> correspondents;

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
    @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false)
    private Set<ActiveStage> activeStages;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    @Where(clause = "deleted_on IS NULL")
    private Set<CaseDataTag> tag;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    private Set<SomuItem> somu_items;

    public void update(Map<String, String> newData) {
        if (newData!=null && newData.size() > 0) {
            this.dataMap.putAll(newData);
        }
    }

    public void update(String key, String value) {
        this.dataMap.put(key, value);
    }

    public String getData(String key) {
        return this.dataMap.getOrDefault(key, null);
    }

    public void appendTags(List<String> tags) {
        if (tag==null) {
            tag = new HashSet<>();
        }
        tag.addAll(tags.stream().map(tagStr -> new CaseDataTag(this.uuid, tagStr)).toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        CaseData caseData = (CaseData) o;
        return uuid.equals(caseData.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
