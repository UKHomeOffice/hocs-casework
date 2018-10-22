package uk.gov.digital.ho.hocs.casework.casedetails.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "case_input_data")
@NoArgsConstructor
@AllArgsConstructor
public class InputData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "data")
    private String data;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;


    public InputData(UUID caseUUID) {
        if (caseUUID == null) {
            throw new EntityCreationException("Cannot create InputData(null).");
        }
        this.data = "{}";
        this.caseUUID = caseUUID;
        this.created = LocalDateTime.now();
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
}