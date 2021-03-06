package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetAuditResponse {

    @JsonProperty(value = "uuid")
    private UUID uuid;

    @JsonProperty(value = "caseUUID")
    private UUID caseUUID;

    @JsonProperty(value = "stageUUID")
    private UUID stageUUID;

    @JsonProperty(value = "correlation_id")
    private String correlationID;

    @JsonProperty(value = "raising_service")
    private String raisingService;

    @JsonProperty(value = "audit_payload")
    private String auditPayload;

    @JsonProperty(value = "namespace")
    private String namespace;

    @JsonProperty(value = "audit_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z", timezone = "UTC")
    private ZonedDateTime auditTimestamp;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "user_id")
    private String userID;

}