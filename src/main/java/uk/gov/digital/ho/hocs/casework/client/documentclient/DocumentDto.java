package uk.gov.digital.ho.hocs.casework.client.documentclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor()
@Getter
@Slf4j
public class DocumentDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;

    @JsonProperty("type")
    private String type;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("updated")
    private LocalDateTime updated;

    @JsonProperty("deleted")
    private Boolean deleted;

}