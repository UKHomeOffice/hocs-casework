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

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void calculateWhenUnclaimedOrUnpopulatedReturnsZero(){
        Map<String,String> dataMap = new HashMap();
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
    public void calculateWhenNoClaimedGivenReturnsSumIncludingNullClaim(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("AaCheck", "");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "");
        dataMap.put("CcValue", "3.03");
        DataTotal dataTotal = new DataTotal();
        Map<String, String> addFields = new HashMap();
        addFields.put(null, "AaValue");
        addFields.put("", "BbValue");
        addFields.put(" ", "CcValue");

        BigDecimal result = dataTotal.calculate(dataMap, addFields, makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("6.06"));
    }

    @Test
    public void calculateWhenClaimedAndPopulatedReturnsSum(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("AaCheck", "Yes");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "Yes");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "Yes");
        dataMap.put("CcValue", "3.03");
        dataMap.put("DdCheck", "");
        dataMap.put("DdMinus", "4.04");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("6.06"));
    }

    @Test
    public void calculateWhenAddAndDeductReturnsSum(){
        Map<String,String> dataMap = new HashMap();
        dataMap.put("AaCheck", "Yes");
        dataMap.put("AaValue", "1.01");
        dataMap.put("BbCheck", "Yes");
        dataMap.put("BbValue", "2.02");
        dataMap.put("CcCheck", "Yes");
        dataMap.put("CcValue", "3.03");
        dataMap.put("DdCheck", "Yes");
        dataMap.put("DdMinus", "4.04");
        DataTotal dataTotal = new DataTotal();

        BigDecimal result = dataTotal.calculate(dataMap, makeAddFields(), makeSubFields());

        assertThat(result).isEqualTo(new BigDecimal("2.02"));
    }

    private Map<String, String> makeAddFields(){
        Map<String, String> addFields = new HashMap();
        addFields.put("AaCheck", "AaValue");
        addFields.put("BbCheck", "BbValue");
        addFields.put("CcCheck", "CcValue");
        return addFields;
    }

    private Map<String, String> makeSubFields(){
        Map<String, String> subFields = new HashMap();
        subFields.put("DdCheck", "DdMinus");
        subFields.put("EeCheck", "EeMinus");
        return subFields;
    }
}
