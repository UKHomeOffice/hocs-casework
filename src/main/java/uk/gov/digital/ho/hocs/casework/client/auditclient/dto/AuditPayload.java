package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

public interface AuditPayload {

    @AllArgsConstructor
    @Getter
    class CaseReference {
        private String caseReference;
    }

    @AllArgsConstructor
    @Getter
    class Topic {
        private String topicName;
    }

    @AllArgsConstructor
    @Getter
    class StageTeamAllocation {
        private UUID stageUUID;
        private UUID teamUUID;
        private String stage;
    }

    @AllArgsConstructor
    @Getter
    class StageUserAllocation {
        private UUID stageUUID;
        private UUID userUUID;
        private String stage;
    }

    @AllArgsConstructor
    @Getter
    class CaseNote {
        private String caseNote;
    }

    @AllArgsConstructor
    @Getter
    class Document {
        private UUID documentUUID;
        private String documentTitle;
        private String DocumentType;
    }
}
