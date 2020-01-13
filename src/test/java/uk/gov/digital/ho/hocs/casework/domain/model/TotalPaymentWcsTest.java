package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TotalPaymentWcsTest {

    @Test
    public void calculateAwardWhenEmptyReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculateAward(dataMap);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateAwardWhenUnclaimedOrUnpopulatedReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("LegalFeesClaimed", "");
        dataMap.put("LegalFeesAward", "1.01");
        dataMap.put("RemovalClaimed", "");
        dataMap.put("RemovalAward", "2.02");
        dataMap.put("EmploymentClaimed", "");
        dataMap.put("EmploymentAward", "3.03");
        dataMap.put("TaxCreditClaimed", "");
        dataMap.put("TaxCreditAward", "4.04");
        dataMap.put("BenefitsClaimed", "");
        dataMap.put("BenefitsAward", "5.05");
        dataMap.put("HousingClaimed", "");
        dataMap.put("HousingAward", "6.06");
        dataMap.put("HealthClaimed", "Yes");
        dataMap.put("HealthAward", "");
        dataMap.put("BankingClaimed", "Yes");
        dataMap.put("BankingAward", "");
        dataMap.put("DrivingClaimed", "Yes");
        dataMap.put("DrivingAward", "");
        dataMap.put("HomelessClaimed", "Yes");
        dataMap.put("HomelessAward", "");
        dataMap.put("ImpactClaimed", "Yes");
        dataMap.put("ImpactAward", "");
        dataMap.put("DiscretionClaimed", "Yes");
        dataMap.put("DiscretionAward", "");
        dataMap.put("DvlaClaimed", "Yes");
        dataMap.put("DvlaAward", "");
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculateAward(dataMap);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateAwardWhenClaimedAndPopulatedReturnsSum(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("LegalFeesClaimed", "Yes");
        dataMap.put("LegalFeesAward", "1.01");
        dataMap.put("RemovalClaimed", "Yes");
        dataMap.put("RemovalAward", "2.02");
        dataMap.put("EmploymentClaimed", "Yes");
        dataMap.put("EmploymentAward", "3.03");
        dataMap.put("TaxCreditClaimed", "Yes");
        dataMap.put("TaxCreditAward", "4.04");
        dataMap.put("BenefitsClaimed", "Yes");
        dataMap.put("BenefitsAward", "5.05");
        dataMap.put("HousingClaimed", "Yes");
        dataMap.put("HousingAward", "6.06");
        dataMap.put("HealthClaimed", "Yes");
        dataMap.put("HealthAward", "7.07");
        dataMap.put("BankingClaimed", "Yes");
        dataMap.put("BankingAward", "8.08");
        dataMap.put("DrivingClaimed", "Yes");
        dataMap.put("DrivingAward", "9.09");
        dataMap.put("HomelessClaimed", "Yes");
        dataMap.put("HomelessAward", "10.01");
        dataMap.put("ImpactClaimed", "Yes");
        dataMap.put("ImpactAward", "11.01");
        dataMap.put("DiscretionClaimed", "Yes");
        dataMap.put("DiscretionAward", "12.02");
        dataMap.put("DvlaClaimed", "Yes");
        dataMap.put("DvlaAward", "13.03");
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculateAward(dataMap);

        assertThat(result).isEqualTo(new BigDecimal("91.52"));
    }

    @Test
    public void calculatePaidWhenEmptyReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculatePaid(dataMap);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculatePaidWhenUnclaimedOrUnpopulatedReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("LegalFeesClaimed", "");
        dataMap.put("LegalFeesAward", "1.01");
        dataMap.put("RemovalClaimed", "");
        dataMap.put("RemovalAward", "2.02");
        dataMap.put("EmploymentClaimed", "");
        dataMap.put("EmploymentAward", "3.03");
        dataMap.put("TaxCreditClaimed", "");
        dataMap.put("TaxCreditAward", "4.04");
        dataMap.put("BenefitsClaimed", "");
        dataMap.put("BenefitsAward", "5.05");
        dataMap.put("HousingClaimed", "");
        dataMap.put("HousingAward", "6.06");
        dataMap.put("HealthClaimed", "Yes");
        dataMap.put("HealthAward", "");
        dataMap.put("BankingClaimed", "Yes");
        dataMap.put("BankingAward", "");
        dataMap.put("DrivingClaimed", "Yes");
        dataMap.put("DrivingAward", "");
        dataMap.put("HomelessClaimed", "Yes");
        dataMap.put("HomelessAward", "");
        dataMap.put("ImpactClaimed", "Yes");
        dataMap.put("ImpactAward", "");
        dataMap.put("DiscretionClaimed", "Yes");
        dataMap.put("DiscretionAward", "");
        dataMap.put("DvlaClaimed", "Yes");
        dataMap.put("DvlaAward", "");
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculatePaid(dataMap);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculatePaidWhenClaimedAndPopulatedReturnsSum(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("LegalFeesClaimed", "Yes");
        dataMap.put("LegalFeesPaid", "1.01");
        dataMap.put("RemovalClaimed", "Yes");
        dataMap.put("RemovalPaid", "2.02");
        dataMap.put("EmploymentClaimed", "Yes");
        dataMap.put("EmploymentPaid", "3.03");
        dataMap.put("TaxCreditClaimed", "Yes");
        dataMap.put("TaxCreditPaid", "4.04");
        dataMap.put("BenefitsClaimed", "Yes");
        dataMap.put("BenefitsPaid", "5.05");
        dataMap.put("HousingClaimed", "Yes");
        dataMap.put("HousingPaid", "6.06");
        dataMap.put("HealthClaimed", "Yes");
        dataMap.put("HealthPaid", "7.07");
        dataMap.put("BankingClaimed", "Yes");
        dataMap.put("BankingPaid", "8.08");
        dataMap.put("DrivingClaimed", "Yes");
        dataMap.put("DrivingPaid", "9.09");
        dataMap.put("HomelessClaimed", "Yes");
        dataMap.put("HomelessPaid", "10.01");
        dataMap.put("ImpactClaimed", "Yes");
        dataMap.put("ImpactPaid", "11.01");
        dataMap.put("DiscretionClaimed", "Yes");
        dataMap.put("DiscretionPaid", "12.02");
        dataMap.put("DvlaClaimed", "Yes");
        dataMap.put("DvlaPaid", "13.03");
        TotalPaymentWcs totalPaymentWcs = new TotalPaymentWcs();

        BigDecimal result = totalPaymentWcs.calculatePaid(dataMap);

        assertThat(result).isEqualTo(new BigDecimal("91.52"));
    }
}
