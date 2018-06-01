package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.Setter;

import java.util.Map;

@Setter
public class RshReportLine {


    public String CaseType;


    public static RshReportLine from(Map<String,String> map) {
        RshReportLine reportLine = new RshReportLine();
        reportLine.setCaseType(map.getOrDefault("Case_Type",""));

        return reportLine;
    }
}
