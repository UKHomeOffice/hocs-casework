package uk.gov.digital.ho.hocs.casework.casedetails.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "stage_data")
@NoArgsConstructor
public class StageData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    private String stageType;

    @Column(name ="data")
    @Getter
    private String data;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "case_reference")
    @Getter
    private String caseReference;

    @Column(name = "team_uuid")
    @Getter
    private UUID teamUUID;

    @Column(name = "user_uuid")
    @Getter
    private UUID userUUID;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    public StageData(CaseData caseData, StageType type, Map<String, String> dataMap, ObjectMapper objectMapper) {
        this.uuid = UUID.randomUUID();
        this.stageType = type.toString();
        this.data = getDataString(dataMap, objectMapper);
        this.caseUUID = caseData.getUuid();
        this.caseReference = caseData.getReference();
    }

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Object Mapper failed to write value!");
        }
    }

    public void updateData(Map<String, String> newData, ObjectMapper objectMapper) {
        HashMap<String, String> dataMap;
        try {
            dataMap = objectMapper.readValue(this.data, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new EntityCreationException("Object Mapper failed to read value!");
        }
        dataMap.putAll(newData);
        this.data = getDataString(dataMap, objectMapper);
    }

    public StageType getType() {
        return StageType.valueOf(this.stageType);
    }

    public void allocate(UUID teamUUID, UUID userUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
    }

    public void unallocate() {
        this.teamUUID = null;
        this.userUUID = null;
    }
}