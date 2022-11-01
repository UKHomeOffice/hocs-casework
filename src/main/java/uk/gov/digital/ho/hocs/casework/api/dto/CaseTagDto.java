package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataTag;

import java.time.LocalDateTime;
import java.util.UUID;

public class CaseTagDto {

    @JsonProperty("caseUuid")
    private UUID caseUuid;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("created")
    private LocalDateTime createdAt;

    public CaseTagDto(UUID caseUuid, String tag, LocalDateTime createdAt) {
        this.caseUuid = caseUuid;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    private CaseTagDto() {}

    public static CaseTagDto from(CaseDataTag caseDataTag) {
        return new CaseTagDto(caseDataTag.getCaseUuid(), caseDataTag.getTag(), caseDataTag.getCreatedAtDate());
    }

    public UUID getCaseUuid() {
        return caseUuid;
    }

    public String getTag() {
        return tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
