package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExportLine {

    @Getter
    private final LinkedHashMap<String, String> lineSchema = new LinkedHashMap<>();

    private ExportLine(String schema) {
        String[] headers = schema.split(",");
        for (String h : headers) {
            lineSchema.put(h, "");
        }
    }

    private void applyData(Map<String, String> values) {
        values.forEach(lineSchema::replace);
    }

    public static ExportLine from(String schema, Map<String, String> map) {
        ExportLine exportLine = new ExportLine(schema);
        exportLine.applyData(map);
        return exportLine;
    }
}