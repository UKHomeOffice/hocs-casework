package uk.gov.digital.ho.hocs.casework.casedetails.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Getter
    @Column(name = "data")
    private String data;

    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    public CaseData(CaseDataType type, Long caseNumber) {
        if (type == null || caseNumber == null) {
            throw new EntityCreationException("Cannot create InputData(%s,%s).", type, caseNumber);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.type = type.toString();
        this.reference = String.format("%S/%07d/%ty", this.type, caseNumber, this.created);
        this.data = "{}";
    }

    public String getTypeString() {
        return this.type;
    }

    public CaseDataType getType() {
        return CaseDataType.valueOf(this.type);
    }

    public void updateData(Map<String, String> newData, ObjectMapper objectMapper) {
        HashMap<String, String> dataMap;
        if (newData != null && !newData.isEmpty()) {
            try {
                dataMap = objectMapper.readValue(this.data, new TypeReference<Map<String, String>>() {
                });
            } catch (Exception e) {
                throw new EntityCreationException("Object Mapper failed to read value!");
            }
            dataMap.putAll(newData);
            this.data = getDataString(dataMap, objectMapper);
        }
    }

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Object Mapper failed to write value!");
        }
    }
}