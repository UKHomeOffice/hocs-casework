package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SomuTypeDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseType")
    private String caseType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("active")
    private boolean active;

    @JsonDeserialize(using = MapDeserializer.class)
    @JsonProperty("schema")
    private Map<String, Object> schema;

    private static class MapDeserializer extends JsonDeserializer<Map<String, Object>> {

        @Override
        public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return (new ObjectMapper()).readValue(p.getValueAsString(), new TypeReference<>() {});
        }

    }

}
