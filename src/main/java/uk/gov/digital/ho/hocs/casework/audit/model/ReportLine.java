package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportLine {

    @Getter
    // A LinkedHashMap preserves insertion order, which means the column values are in the same place on each row and between extracts.
    private final LinkedHashMap<String, String> lineData = new LinkedHashMap<>();

    private ReportLine(String header, Map<String, String> values) {
        // Put the headings in as the key and then replace the values to maintain the order
        String[] headers = header.split(",");
        for (String h : headers) {
            lineData.put(h, "");
        }
        values.forEach(lineData::replace);
    }

    public static ReportLine from(String header, Map<String, String> values) {
        return new ReportLine(header, values);
    }
}