package uk.gov.digital.ho.hocs.casework.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

public class DataTotal {

    public BigDecimal calculate(Map<String,String> dataMap, Map<String,String> addFields, Map<String,String> subFields){
        BigDecimal total = BigDecimal.ZERO;
        for(Map.Entry<String, String> field : addFields.entrySet()) {
            total = total.add(parseCurrency(dataMap, field.getKey(), field.getValue()));
        }
        for(Map.Entry<String, String> field: subFields.entrySet()) {
            total = total.subtract(parseCurrency(dataMap, field.getKey(), field.getValue()));
        }
        return total;
    }

    private BigDecimal parseCurrency(Map<String,String> dataMap, String claimedKey, String valueKey){
        try {
            if (StringUtils.isBlank(claimedKey) || dataMap.getOrDefault(claimedKey, "").toUpperCase().equals("YES")) {
                return new BigDecimal(dataMap.getOrDefault(valueKey, "0"));
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
