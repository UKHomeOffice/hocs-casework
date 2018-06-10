package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class RshReportLine {

    private static final String RSH_SCHEMA = "Case_Type,Case_Reference,Stage_legacy-reference,Case_UUID,Case_Created,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Created,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email";

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
