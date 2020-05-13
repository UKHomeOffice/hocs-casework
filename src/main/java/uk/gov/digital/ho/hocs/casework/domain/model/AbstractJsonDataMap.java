package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.casework.util.JsonDataMapUtils;

import java.util.Map;

public abstract class AbstractJsonDataMap {

    public abstract String getData();

    abstract protected void setData(String data);

    public void update(Map<String, String> newData, ObjectMapper objectMapper) {
        setData(JsonDataMapUtils.update(getData(), newData, objectMapper));
    }

    public Map<String, String> getDataMap(ObjectMapper objectMapper) {
        return JsonDataMapUtils.getDataMap(getData(), objectMapper);
    }
}
