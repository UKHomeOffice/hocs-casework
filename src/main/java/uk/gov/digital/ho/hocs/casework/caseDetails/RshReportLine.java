package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.Getter;

import java.util.*;

public class RshReportLine {

    private static final String RSH_SCHEMA  = "Case_Type,Case_Reference,Case_UUID,Case_Created,OnlyStage_Name,OnlyStage_UUID,OnlyStage_SchemaVersion,OnlyStage_Created,OnlyStage_who-calling,OnlyStage_rep-title,OnlyStage_rep-first-name,OnlyStage_rep-middle-name,OnlyStage_rep-last-name,OnlyStage_rep-mob,OnlyStage_rep-tel,OnlyStage_rep-address-1,OnlyStage_rep-address-2,OnlyStage_rep-address-town,OnlyStage_rep-post-code,OnlyStage_rep-email,OnlyStage_rep-org,OnlyStage_rep-relationship,OnlyStage_rep-calledfrom,OnlyStage_contact-method-helpline,OnlyStage_contact-method-method-mp,OnlyStage_contact-method-media,OnlyStage_contact-method-ie,OnlyStage_contact-method-email,OnlyStage_contact-method-als,OnlyStage_contact-method-internal,OnlyStage_contact-method-external,OnlyStage_call-regarding-citizenship,OnlyStage_call-regarding-settled,OnlyStage_call-regarding-compensation,OnlyStage_call-regarding-other,OnlyStage_title,OnlyStage_first-name,OnlyStage_middle-name,OnlyStage_last-name,OnlyStage_maiden-name,OnlyStage_date-of-birth,OnlyStage_nationality-birth,OnlyStage_nationality-current,OnlyStage_email,OnlyStage_mob,OnlyStage_tel,OnlyStage_address-1,OnlyStage_address-2,OnlyStage_address-town,OnlyStage_post-code,OnlyStage_dependents,OnlyStage_dependents-how-many,OnlyStage_high-profile,OnlyStage_safeguarding,OnlyStage_safeguarding-details,OnlyStage_share-data,OnlyStage_landing-date,OnlyStage_cohort,OnlyStage_date-left,OnlyStage_country-based,OnlyStage_date-last-travelled,OnlyStage_ref-cid,OnlyStage_ref-crs,OnlyStage_ref-ho,OnlyStage_ref-other,OnlyStage_relations,OnlyStage_undocumented-family,OnlyStage_other-circs,OnlyStage_nino,OnlyStage_employment,OnlyStage_employment-details,OnlyStage_education,OnlyStage_education-details,OnlyStage_tax,OnlyStage_tax-details,OnlyStage_health,OnlyStage_health-details,OnlyStage_id-docs,OnlyStage_id-docs-details,OnlyStage_earliest-docs,OnlyStage_aob,OnlyStage_travel-to-psc,OnlyStage_psc-travel-details,OnlyStage_psc-availability,OnlyStage_psc-location,OnlyStage_psc-special-reqs,OnlyStage_psc-paid-travel,OnlyStage_travel-ref,OnlyStage_travel-cost,OnlyStage_psc-date,OnlyStage_psc-outcome,OnlyStage_psc-details,OnlyStage_psc-followup,OnlyStage_psc-followup-details,OnlyStage_additional-notes,OnlyStage_mp,OnlyStage_mp-details,OnlyStage_media,OnlyStage_media-details,OnlyStage_outcome,OnlyStage_notify-email";

    @Getter
    private LinkedHashMap<String, String> lineSchema = new LinkedHashMap<>();

    private RshReportLine() {
        List<String> headers = Arrays.asList(RSH_SCHEMA.split(","));
        headers.stream().forEach(h -> lineSchema.put(h, ""));
    }

    public void applyData(Map<String,String> values) {
        values.forEach((k,v) -> lineSchema.replace(k,v));
    }

    public static RshReportLine from(Map<String,String> map) {
        RshReportLine reportLine = new RshReportLine();
        reportLine.applyData(map);
        return reportLine;
    }
}
