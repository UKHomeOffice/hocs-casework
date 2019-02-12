package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DATA_JSON_PARSE_ERROR;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

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

    @Setter
    @Getter
    @Column(name = "deleted")
    private boolean deleted;

    @Getter
    @Column(name = "data")
    private String data = "{}";

    @Setter
    @Getter
    @Column(name = "primary_topic_uuid")
    private UUID primaryTopicUUID;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_topic_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Topic primaryTopic;

    @Setter
    @Getter
    @Column(name = "primary_correspondent_uuid")
    private UUID primaryCorrespondentUUID;

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_correspondent_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Correspondent primaryCorrespondent;

    @Setter
    @Getter
    @Column(name = "case_deadline")
    private LocalDate caseDeadline;

    @Getter
    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Getter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Set<ActiveStage> activeStages;

    @Getter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Set<CaseNote> caseNotes;

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, ObjectMapper objectMapper, LocalDate caseDeadline, LocalDate dateReceived) {
        this(type, caseNumber, caseDeadline, dateReceived);
        update(data, objectMapper);
    }

    public CaseData(CaseDataType type, Long caseNumber, LocalDate caseDeadline, LocalDate dateReceived) {
        if (type == null || caseNumber == null) {
            throw new ApplicationExceptions.EntityCreationException("Cannot create CaseData", CASE_CREATE_FAILURE);
        }

        this.type = type.getDisplayCode();
        this.reference = generateCaseReference(caseNumber);
        this.uuid = randomUUID(type.getShortCode());
        this.caseDeadline = caseDeadline;
        this.dateReceived = dateReceived;
    }

    public void update(Map<String, String> newData, ObjectMapper objectMapper) {
        if (newData != null && newData.size() > 0) {
            Map<String, String> dataMap = getDataMap(this.data, objectMapper);

            dataMap.putAll(newData);

            this.data = getDataString(dataMap, objectMapper);
        }
    }

    public Map<String,String> getDataMap(ObjectMapper objectMapper) {
        return getDataMap(this.getData(), objectMapper);
    }

    private static Map<String, String> getDataMap(String dataString, ObjectMapper objectMapper) {
        Map<String, String> dataMap;
        try {
            dataMap = objectMapper.readValue(dataString, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityCreationException("Object Mapper failed to read data value!", CASE_DATA_JSON_PARSE_ERROR);
        }
        return dataMap;
    }

    private static String getDataString(Map<String, String> dataMap, ObjectMapper objectMapper) {
        String dataString;
        try {
            dataString = objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityCreationException("Object Mapper failed to write value!", CASE_DATA_JSON_PARSE_ERROR);
        }
        return dataString;
    }

    private String generateCaseReference(Long caseNumber) {
        return String.format("%S/%07d/%ty", this.type, caseNumber, this.created);
    }

    private static UUID randomUUID(String shortCode) {
        if (shortCode != null) {
            String uuid = UUID.randomUUID().toString().substring(0, 33);
            return UUID.fromString(uuid.concat(shortCode));
        } else {
            throw new ApplicationExceptions.EntityCreationException("shortCode is null", CASE_CREATE_FAILURE);
        }
    }

}
