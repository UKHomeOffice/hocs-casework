package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExportLine {

    @Getter
    private final LinkedHashMap<String, String> lineSchema = new LinkedHashMap<>();

    private ExportLine(String header, Map<String, String> values) {
        String[] headers = header.split(",");
        for (String h : headers) {
            lineSchema.put(h, "");
        }
        values.forEach(lineSchema::replace);
    }

    public static ExportLine from(String schema, Map<String, String> map) {
        return new ExportLine(schema, map);
    }
}