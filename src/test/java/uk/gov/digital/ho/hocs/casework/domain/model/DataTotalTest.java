package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTotalTest {

    @Test
    public void calculateWhenEmptyReturnsZero() {
        Map<String, String> dataMap = new HashMap();
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateWhenUnclaimedOrUnpopulatedReturnsZero() {
        Map<String, String> dataMap = new HashMap();
        dataMap.put("AaCheck", "");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "");
        dataMap.put("CcValue", "3.03");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateWhenNoClaimedGivenReturnsSumIncludingNullClaim() {
        Map<String, String> dataMap = new HashMap();
        dataMap.put("AaCheck", "");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "");
        dataMap.put("CcValue", "3.03");
        DataTotal dataTotal = new DataTotal();
        Map<String, List<String>> addFields = new HashMap();
        addFields.put(null, List.of("AaValue"));
        addFields.put("", List.of("BbValue"));
        addFields.put(" ", List.of("CcValue"));

        BigDecimal result = dataTotal.calculate(dataMap, addFields, makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("6.06"));
    }

    @Test
    public void calculateWhenClaimedAndPopulatedReturnsSum() {
        Map<String, String> dataMap = new HashMap();
        dataMap.put("AaCheck", "Yes");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "Yes");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "Yes");
        dataMap.put("CcValue1", "3.03");
        dataMap.put("CcValue2", "1.00");
        dataMap.put("DdCheck", "");
        dataMap.put("DdMinus", "4.04");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("7.06"));
    }

    @Test
    public void calculateWhenAddAndDeductReturnsSum() {
        Map<String, String> dataMap = new HashMap();
        dataMap.put("AaCheck", "Yes");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "Yes");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "Yes");
        dataMap.put("CcValue1", "3.03");
        dataMap.put("CcValue2", "1.00");
        dataMap.put("DdCheck", "Yes");
        dataMap.put("DdMinus", "4.04");
        dataMap.put("EeCheck", "Yes");
        dataMap.put("EeMinus1", "1.00");
        dataMap.put("EeMinus2", "0.01");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("2.01"));
    }

    private Map<String, List<String>> makeAddFields() {
        Map<String, List<String>> addFields = new HashMap();
        addFields.put("AaCheck", List.of("AaValue"));
        addFields.put("BbCheck", List.of("BbValue"));
        addFields.put("CcCheck", List.of("CcValue1", "CcValue2"));
        return addFields;
    }

    private Map<String, List<String>> makeSubFields() {
        Map<String, List<String>> subFields = new HashMap();
        subFields.put("DdCheck", List.of("DdMinus"));
        subFields.put("EeCheck", List.of("EeMinus1", "EeMinus2"));
        return subFields;
    }

}
