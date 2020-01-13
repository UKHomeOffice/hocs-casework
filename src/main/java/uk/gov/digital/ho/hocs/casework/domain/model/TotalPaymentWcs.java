package uk.gov.digital.ho.hocs.casework.domain.model;

import java.math.BigDecimal;
import java.util.Map;

public class TotalPaymentWcs {

    public BigDecimal calculateAward(Map<String,String> dataMap){
        return
                parseCurrency(dataMap, "LegalFeesClaimed", "LegalFeesAward")
                        .add(parseCurrency(dataMap, "RemovalClaimed", "RemovalAward"))
                        .add(parseCurrency(dataMap, "EmploymentClaimed", "EmploymentAward"))
                        .add(parseCurrency(dataMap, "TaxCreditClaimed", "TaxCreditAward"))
                        .add(parseCurrency(dataMap, "BenefitsClaimed", "BenefitsAward"))
                        .add(parseCurrency(dataMap, "HousingClaimed", "HousingAward"))
                        .add(parseCurrency(dataMap, "HealthClaimed", "HealthAward"))
                        .add(parseCurrency(dataMap, "BankingClaimed", "BankingAward"))
                        .add(parseCurrency(dataMap, "DrivingClaimed", "DrivingAward"))
                        .add(parseCurrency(dataMap, "HomelessClaimed", "HomelessAward"))
                        .add(parseCurrency(dataMap, "ImpactClaimed", "ImpactAward"))
                        .add(parseCurrency(dataMap, "DiscretionClaimed", "DiscretionAward"))
                        .add(parseCurrency(dataMap, "DvlaClaimed", "DvlaAward"));
    }

    public BigDecimal calculatePaid(Map<String,String> dataMap){
        return
                parseCurrency(dataMap, "LegalFeesClaimed", "LegalFeesPaid")
                        .add(parseCurrency(dataMap, "RemovalClaimed", "RemovalPaid"))
                        .add(parseCurrency(dataMap, "EmploymentClaimed", "EmploymentPaid"))
                        .add(parseCurrency(dataMap, "TaxCreditClaimed", "TaxCreditPaid"))
                        .add(parseCurrency(dataMap, "BenefitsClaimed", "BenefitsPaid"))
                        .add(parseCurrency(dataMap, "HousingClaimed", "HousingPaid"))
                        .add(parseCurrency(dataMap, "HealthClaimed", "HealthPaid"))
                        .add(parseCurrency(dataMap, "BankingClaimed", "BankingPaid"))
                        .add(parseCurrency(dataMap, "DrivingClaimed", "DrivingPaid"))
                        .add(parseCurrency(dataMap, "HomelessClaimed", "HomelessPaid"))
                        .add(parseCurrency(dataMap, "ImpactClaimed", "ImpactPaid"))
                        .add(parseCurrency(dataMap, "DiscretionClaimed", "DiscretionPaid"))
                        .add(parseCurrency(dataMap, "DvlaClaimed", "DvlaPaid"));
    }

    private BigDecimal parseCurrency(Map<String,String> dataMap, String claimedKey, String valueKey){
        try {
            if (dataMap.getOrDefault(claimedKey, "").toUpperCase().equals("YES")) {
                return new BigDecimal(dataMap.getOrDefault(valueKey, "0"));
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
