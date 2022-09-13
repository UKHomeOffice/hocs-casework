package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class CaseSummarySomuItems {

    private Map<String, Object> schema;

    private List<Map<String, String>> items;

    public CaseSummarySomuItems(Map<String, Object> schema) {
        this.schema = schema;

        items = new ArrayList<>();
    }

    public void addItem(String item) throws JsonProcessingException {
        this.items.add(new ObjectMapper().readValue(item, new TypeReference<Map<String, String>>() {}));
    }

}