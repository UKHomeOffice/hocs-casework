package uk.gov.digital.ho.hocs.casework.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "case_data_tag")
public class CaseDataTag implements Serializable {

    @Id
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "case_uuid")
    private UUID caseUuid;

    @Column(name = "tag")
    private String tag;

    @Column(name = "created_at")
    private LocalDateTime createdAtDate;

    @Column(name = "deleted_on")
    private LocalDateTime deletedOnDate;

    public CaseDataTag(UUID caseUuid, String tag) {
        this.uuid = UUID.randomUUID();
        this.caseUuid = caseUuid;
        this.tag = tag;
    }

    protected CaseDataTag() { }

    public UUID getCaseUuid() {
        return caseUuid;
    }

    public String getTag() {
        return tag;
    }

    public LocalDateTime getCreatedAtDate() {
        return createdAtDate;
    }

    @PrePersist
    public void prePersist() {
        createdAtDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CaseDataTag that = (CaseDataTag) o;
        return Objects.equals(caseUuid, that.caseUuid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseUuid, tag);
    }

}
