package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.AddressDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    class SomuItemUpdate {
        private UUID uuid;
        private UUID caseUuid;
        private UUID somuUuid;
        private String data;
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
}
