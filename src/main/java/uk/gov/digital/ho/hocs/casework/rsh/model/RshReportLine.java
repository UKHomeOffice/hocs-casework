package uk.gov.digital.ho.hocs.casework.rsh.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class RshReportLine {

    @Getter
    private final LinkedHashMap<String, String> lineSchema = new LinkedHashMap<>();

    private RshReportLine(String schema) {
        String[] headers = schema.split(",");
        for (String h : headers) {
            lineSchema.put(h, "");
        }
    }

    private void applyData(Map<String, String> values) {
        values.forEach(lineSchema::replace);
    }

    public static RshReportLine from(String schema, Map<String, String> map) {
        RshReportLine reportLine = new RshReportLine(schema);
        reportLine.applyData(map);
        return reportLine;
    }
}