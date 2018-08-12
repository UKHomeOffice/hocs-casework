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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "case_input_data")
@NoArgsConstructor
public class CaseInputData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "data")
    @Getter
    private String data;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "type")
    private String caseType;

    @Column(name = "reference")
    @Getter
    private String reference;

    public CaseInputData(UUID caseUUID, CaseType caseType, Long caseNumber, LocalDateTime localDateTime) {
        this.caseUUID = caseUUID;
        this.caseType = caseType.toString();
        this.reference = String.format("%s/%07d/%s", caseType.toString(), caseNumber, localDateTime.format(DateTimeFormatter.ofPattern("yy")));
        this.data = "{}";
    }

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Object Mapper failed to write value!");
        }
    }

    public CaseType getType() {
        return CaseType.valueOf(this.caseType);
    }

    public String getTypeString() {
        return this.caseType;
    }

    public void updateData(Map<String, String> newData, ObjectMapper objectMapper) {
        HashMap<String, String> dataMap;
        if (this.data != null) {
            try {
                dataMap = objectMapper.readValue(this.data, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new EntityCreationException("Object Mapper failed to read value!");
            }
        } else {
            dataMap = new HashMap<>();
        }
        dataMap.putAll(newData);
        this.data = getDataString(dataMap, objectMapper);
    }
}