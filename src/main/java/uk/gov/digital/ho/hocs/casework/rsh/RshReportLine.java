package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class RshReportLine {

    private static final String RSH_SCHEMA  = "Case_Type,Case_Reference,OnlyStage_legacy-reference,Case_UUID,Case_Created,OnlyStage_Name,OnlyStage_UUID,OnlyStage_SchemaVersion,OnlyStage_Created,OnlyStage_who-calling,OnlyStage_rep-first-name,OnlyStage_rep-last-name,OnlyStage_rep-org,OnlyStage_rep-relationship,OnlyStage_rep-calledfrom,OnlyStage_contact-method-helpline,OnlyStage_contact-method-method-mp,OnlyStage_contact-method-media,OnlyStage_contact-method-ie,OnlyStage_contact-method-email,OnlyStage_contact-method-als,OnlyStage_contact-method-internal,OnlyStage_contact-method-external,OnlyStage_call-regarding-citizenship,OnlyStage_call-regarding-settled,OnlyStage_call-regarding-compensation,OnlyStage_call-regarding-other,OnlyStage_first-name,OnlyStage_middle-name,OnlyStage_last-name,OnlyStage_date-of-birth,OnlyStage_nationality-birth,OnlyStage_nationality-current,OnlyStage_address-1,OnlyStage_address-2,OnlyStage_address-town,OnlyStage_post-code,OnlyStage_dependents,OnlyStage_dependents-how-many,OnlyStage_high-profile,OnlyStage_safeguarding,OnlyStage_share-data,OnlyStage_landing-date-day,OnlyStage_landing-date-month,OnlyStage_landing-date-year,OnlyStage_cohort,OnlyStage_date-left,OnlyStage_country-based,OnlyStage_date-last-travelled,OnlyStage_nino,OnlyStage_employment,OnlyStage_education,OnlyStage_tax,OnlyStage_health,OnlyStage_id-docs,OnlyStage_travel-to-psc,OnlyStage_psc-location,OnlyStage_psc-date,OnlyStage_psc-outcome,OnlyStage_psc-followup,OnlyStage_mp,OnlyStage_media,OnlyStage_outcome,OnlyStage_notify-email";

    @Getter
    private final LinkedHashMap<String, String> lineSchema = new LinkedHashMap<>();

    private RshReportLine() {
        String[] headers = RSH_SCHEMA.split(",");
        for (String h : headers) {
            lineSchema.put(h, "");
        }
    }

    private void applyData(Map<String, String> values) {
        values.forEach(lineSchema::replace);
    }

    public static RshReportLine from(Map<String,String> map) {
        RshReportLine reportLine = new RshReportLine();
        reportLine.applyData(map);
        return reportLine;
    }
}
