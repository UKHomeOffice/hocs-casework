package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid = UUID.randomUUID();

    @Getter
    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Getter
    @Column(name = "data")
    private String data = "{}";

    @Getter
    @Column(name = "primary_topic")
    private String primaryTopic;

    @Getter
    @Column(name = "primary_correspondent")
    private String primaryCorrespondent;

    @Getter
    @Column(name = "primary_response")
    private String primaryResponse;

    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, ObjectMapper objectMapper) {
        validateCreate(type, caseNumber);

        this.type = type.toString();
        this.reference = String.format("%S/%07d/%ty", this.type, caseNumber, this.created);
        update(data, objectMapper);
    }

    private static void validateCreate(CaseDataType type, Long caseNumber) {
        if (type == null || caseNumber == null) {
            throw new EntityCreationException("Cannot create CaseData (%s,%s).", type, caseNumber);
        }
    }

    public CaseDataType getCaseDataType() {
        return CaseDataType.valueOf(this.type);
    }

    public void update(Map<String, String> newData, ObjectMapper objectMapper) {

        if (newData != null) {
            this.data = updateDataString(newData, objectMapper);
        }
    }

    private String updateDataString(Map<String, String> stageData, ObjectMapper objectMapper) {
        HashMap<String, String> dataMap;
        try {
            dataMap = objectMapper.readValue(this.data, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new EntityCreationException("Object Mapper failed to read data value!");
        }
        dataMap.putAll(stageData);

        try {
            return objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            throw new EntityCreationException("Object Mapper failed to write value!");
        }
    }

}