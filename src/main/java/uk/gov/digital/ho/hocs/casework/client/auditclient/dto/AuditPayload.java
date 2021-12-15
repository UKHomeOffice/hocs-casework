package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.AddressDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        @JsonProperty("data")
        private Map<String,String> data;

        @JsonProperty("caseDeadline")
        private LocalDate caseDeadline;

        @JsonProperty("dateReceived")
        private LocalDate dateReceived;

        public static AuditPayload.CreateCaseRequest from(CaseData caseData) {
            return new AuditPayload.CreateCaseRequest(caseData.getUuid(),
                    caseData.getCreated(),
                    caseData.getType(),
                    caseData.getReference(),
                    caseData.getDataMap(),
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

        private UUID uuid;
        private UUID caseTypeActionUuid;
        private LocalDate originalDeadline;
        private LocalDate updatedDeadline;
        private String note;
        private LocalDateTime createTimestamp;
        private String reasons;

        public static AuditPayload.CreateExtensionRequest from(ActionDataDeadlineExtension actionDataDeadlineExtension) {
            return new AuditPayload.CreateExtensionRequest(
                    actionDataDeadlineExtension.getUuid(),
                    actionDataDeadlineExtension.getCaseTypeActionUuid(),
                    actionDataDeadlineExtension.getOriginalDeadline(),
                    actionDataDeadlineExtension.getUpdatedDeadline(),
                    actionDataDeadlineExtension.getNote(),
                    actionDataDeadlineExtension.getCreateTimestamp(),
                    actionDataDeadlineExtension.getReasons()
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

        @JsonProperty("data")
        private Map<String,String> data;

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
                    caseData.getDataMap(),
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
    @Getter
    @Builder
    class AppealItem {

        private UUID uuid;
        private UUID caseTypeActionUuid;
        private String status;
        private LocalDate dateSentRMS;
        private String outcome;
        private String complexCase;
        private String note;
        private String officerType;
        private String officerName;
        private String officerDirectorate;
        private LocalDateTime created;
    }

    interface ActionAuditPayload {
        UUID getCaseDataUuid();
        EventType getEventType();
    }

    @AllArgsConstructor
    @Getter
    class ExternalInterestItem implements ActionAuditPayload {

        private UUID uuid;
        private UUID caseDataUuid;
        private String caseType;
        private String interestDetails;
        private EventType eventType;
    }
}
