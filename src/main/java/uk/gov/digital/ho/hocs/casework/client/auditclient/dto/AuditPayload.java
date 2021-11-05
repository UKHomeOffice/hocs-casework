package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.AddressDto;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
public interface AuditPayload {

    @AllArgsConstructor
    @Getter
    class CaseReference {
        private String caseReference;
    }

    @AllArgsConstructor
    @Getter
    class Case {
        private UUID caseUUID;
    }

    @AllArgsConstructor
    @Getter
    class CaseDeleted {
        private UUID caseUUID;
        private Boolean deleted;
    }

    @AllArgsConstructor
    @Getter
    class Topic {
        private UUID topicUuid;
        private String topicName;
    }

    @AllArgsConstructor
    @Getter
    class StageAllocation {
        private UUID stageUUID;
        private UUID allocatedToUUID;
        private String stage;
        private LocalDate deadline;
        private LocalDate deadlineWarning;
    }

    @AllArgsConstructor
    @Getter
    class CaseNote {
        private String caseNote;
    }

    @AllArgsConstructor
    @Getter
    class CaseNoteUpdate {
        private String prevCaseNoteType;
        private String prevText;
        private String caseNoteType;
        private String text;
    }

    @AllArgsConstructor
    @Getter
    class SomuItem {
        @JsonProperty("somuTypeUuid")
        private UUID somuUuid;
    }

    @Getter
    class SomuItemWithData extends SomuItem {
        @JsonProperty("uuid")
        private UUID uuid;
        
        @JsonRawValue
        private String data;

        public SomuItemWithData(UUID somuUuid, UUID uuid, String data) {
            super(somuUuid);
            this.uuid = uuid;
            this.data = data;
        }
    }

    @AllArgsConstructor
    @Getter
    class CreateCaseRequest {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("reference")
        private String reference;

        @JsonRawValue
        private String data;

        @JsonProperty("caseDeadline")
        private LocalDate caseDeadline;

        @JsonProperty("dateReceived")
        private LocalDate dateReceived;

        public static AuditPayload.CreateCaseRequest from(CaseData caseData) {
            return new AuditPayload.CreateCaseRequest(caseData.getUuid(),
                    caseData.getCreated(),
                    caseData.getType(),
                    caseData.getReference(),
                    caseData.getData(),
                    caseData.getCaseDeadline(),
                    caseData.getDateReceived());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class CreateCorrespondentRequest {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("caseUUID")
        private UUID caseUUID;

        @JsonProperty("fullname")
        private String fullname;

        @JsonProperty("organisation")
        private String organisation;

        @JsonProperty("address")
        private AddressDto address;

        @JsonProperty("telephone")
        private String telephone;

        @JsonProperty("email")
        private String email;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("externalKey")
        private String externalKey;

        public static AuditPayload.CreateCorrespondentRequest from(Correspondent correspondent) {
            return new AuditPayload.CreateCorrespondentRequest(
                    correspondent.getUuid(),
                    correspondent.getCreated(),
                    correspondent.getCorrespondentType(),
                    correspondent.getCaseUUID(),
                    correspondent.getFullName(),
                    correspondent.getOrganisation(),
                    AddressDto.from(correspondent),
                    correspondent.getTelephone(),
                    correspondent.getEmail(),
                    correspondent.getReference(),
                    correspondent.getExternalKey()
            );
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class CreateExtensionRequest {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("action_uuid")
        private UUID caseTypeActionUuid;

        @Column(name = "case_data_type")
        private String caseDataType;

        @JsonProperty("caseId")
        private UUID caseId;

        @JsonProperty("originalDeadline")
        private LocalDate originalDeadline;

        @JsonProperty("updatedDeadline")
        private LocalDate updatedDeadline;

        @JsonProperty("note")
        private String note;

        @JsonProperty("created_timestamp")
        private LocalDateTime createTimestamp;

        public static AuditPayload.CreateExtensionRequest from(ActionDataDeadlineExtension actionDataDeadlineExtension) {
            return new AuditPayload.CreateExtensionRequest(
                    actionDataDeadlineExtension.getUuid(),
                    actionDataDeadlineExtension.getCaseTypeActionUuid(),
                    actionDataDeadlineExtension.getCaseDataType(),
                    actionDataDeadlineExtension.getCaseDataUuid(),
                    actionDataDeadlineExtension.getOriginalDeadline(),
                    actionDataDeadlineExtension.getUpdatedDeadline(),
                    actionDataDeadlineExtension.getNote(),
                    actionDataDeadlineExtension.getCreateTimestamp()
            );
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class UpdateCaseRequest {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("reference")
        private String reference;

        @JsonRawValue
        private String data;

        @JsonProperty("primaryTopic")
        private UUID primaryTopic;

        @JsonProperty("primaryCorrespondent")
        private UUID primaryCorrespondent;

        @JsonProperty("caseDeadline")
        private LocalDate caseDeadline;

        @JsonProperty("dateReceived")
        private LocalDate dateReceived;

        public static UpdateCaseRequest from(CaseData caseData) {

            return new UpdateCaseRequest(
                    caseData.getUuid(),
                    caseData.getCreated(),
                    caseData.getType(),
                    caseData.getReference(),
                    caseData.getData(),
                    caseData.getPrimaryTopicUUID(),
                    caseData.getPrimaryCorrespondentUUID(),
                    caseData.getCaseDeadline(),
                    caseData.getDateReceived());
        }
    }

    @AllArgsConstructor
    @Getter
    class GetAuditListResponse {

        private Set<GetAuditResponse> audits;

    }

    @AllArgsConstructor
    class AppealItem {

        private UUID uuid;
        private UUID caseUUID;
        private String caseType;
        private UUID caseTypeActionUUID;
        private String caseTypeActionLabel;
        private String status;
        private LocalDate dateSentRMS;
        private String outcome;
        private String complexCase;
        private String note;
        private String appealOfficerData;

    }
}
