package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Setter
    @Getter
    @Column(name = "priority")
    private boolean priority;

    @Getter
    @Column(name = "data")
    private String data = "{}";

    @Setter
    @Getter
    @Column(name = "primary_topic_uuid")
    private UUID primaryTopicUUID;

    @Setter
    @Getter
    @Column(name = "primary_correspondent_uuid")
    private UUID primaryCorrespondentUUID;

    @Setter
    @Getter
    @Column(name = "case_deadline")
    private LocalDate caseDeadline;

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, ObjectMapper objectMapper, LocalDate caseDeadline) {
        this(type, caseNumber, caseDeadline);
        update(data, objectMapper);
    }

    public CaseData(CaseDataType type, Long caseNumber , LocalDate caseDeadline) {
        if (type == null || caseNumber == null) {
            throw new EntityCreationException("Cannot create CaseData");
        }

        this.type = type.getDisplayCode();
        this.reference = generateCaseReference(caseNumber);
        this.uuid = randomUUID(type.getShortCode());
        this.caseDeadline = caseDeadline;
    }

    private static String getDataString(Map<String, String> dataMap, ObjectMapper objectMapper) {
        String dataString;
        try {
            dataString = objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            throw new EntityCreationException("Object Mapper failed to write value!");
        }
        return dataString;
    }

    private static Map<String, String> getDataMap(String dataString, ObjectMapper objectMapper) {
        Map<String, String> dataMap;
        try {
            dataMap = objectMapper.readValue(dataString, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new EntityCreationException("Object Mapper failed to read data value!");
        }
        return dataMap;
    }

    private String generateCaseReference(Long caseNumber) {
        return String.format("%S/%07d/%ty", this.type, caseNumber, this.created);
    }

    public void update(Map<String, String> newData, ObjectMapper objectMapper) {
        if (newData != null && newData.size() > 0) {
            Map<String, String> dataMap = getDataMap(this.data, objectMapper);

            dataMap.putAll(newData);

            this.data = getDataString(dataMap, objectMapper);
        }
    }

    private static UUID randomUUID(String shortCode) {
        if (shortCode != null) {
            String uuid = UUID.randomUUID().toString().substring(0, 33);
            return UUID.fromString(uuid.concat(shortCode));
        } else {
            throw new EntityCreationException("shortCode is null");
        }
    }

}
