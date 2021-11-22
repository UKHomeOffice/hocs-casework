package uk.gov.digital.ho.hocs.casework.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.util.Map;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DATA_JSON_PARSE_ERROR;

public class JsonDataMapUtils {

    public static Map<String, String> getDataMap(String dataString, ObjectMapper objectMapper) {
        Map<String, String> dataMap;
        try {
            dataMap = objectMapper.readValue(dataString, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityCreationException("Object Mapper failed to read data value!", CASE_DATA_JSON_PARSE_ERROR, e);
        }
        return dataMap;
    }

    public static String getDataString(Map<String, String> dataMap, ObjectMapper objectMapper) {
        String dataString;
        try {
            dataString = objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityCreationException("Object Mapper failed to write value!", CASE_DATA_JSON_PARSE_ERROR, e);
        }
        return dataString;
    }

    public static String update(String originalData, Map<String, String> newData, ObjectMapper objectMapper) {
        var dataMap = getDataMap(originalData, objectMapper);
        if (newData != null && newData.size() > 0) {
            dataMap.putAll(newData);
        }
        return getDataString(dataMap, objectMapper);
    }
}
