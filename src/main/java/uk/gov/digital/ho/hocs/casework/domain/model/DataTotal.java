package uk.gov.digital.ho.hocs.casework.domain.model;

import java.math.BigDecimal;
import java.util.Map;

public class DataTotal {

    public BigDecimal calculate(Map<String,String> dataMap, String checkSuffix, String valueSuffix, String fieldList){
        String[] fields = fieldList.split(",");
        BigDecimal total = BigDecimal.ZERO;
        for(String field : fields) {
            total = total.add(parseCurrency(dataMap, field + checkSuffix, field + valueSuffix));
        }
        return total;
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
