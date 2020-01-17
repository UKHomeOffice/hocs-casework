package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTotalTest {

    @Test
    public void calculateWhenEmptyReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, "Check", "Value", "Aa,Bb,Cc");

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateAwardWhenUnclaimedOrUnpopulatedReturnsZero(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("AaCheck", "");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "");
        dataMap.put("CcValue", "3.03");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, "Check", "Value", "Aa,Bb,Cc");

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateAwardWhenClaimedAndPopulatedReturnsSum(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("AaCheck", "Yes");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "Yes");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "Yes");
        dataMap.put("CcValue", "3.03");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, "Check", "Value", "Aa,Bb,Cc");

        assertThat(result).isEqualTo(new BigDecimal("6.06"));
    }
}
