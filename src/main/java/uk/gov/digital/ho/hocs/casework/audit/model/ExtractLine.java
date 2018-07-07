package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExtractLine {

    @Getter
    // A LinkedHashMap preserves insertion order, which means the column values are in the same place on each row and between extracts.
    private final LinkedHashMap<String, String> lineData = new LinkedHashMap<>();

    private ExtractLine(String header, Map<String, String> values) {
        // Put the headings in as the key and replace the values
        String[] headers = header.split(",");
        for (String h : headers) {
            lineData.put(h, "");
        }
        values.forEach(lineData::replace);
    }

    public static ExtractLine from(String header, Map<String, String> values) {
        return new ExtractLine(header, values);
    }
}