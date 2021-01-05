package uk.gov.digital.ho.hocs.casework.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class DataTotal {

    // This iterates over the fields provided, extracts their values from 'dataMap', then flattens them, before summing them
    BiFunction<Map<String,String>, Map<String, List<String>>, BigDecimal> sumFields = (dataMap, fields) -> {
        return fields.entrySet().stream().flatMap(field -> {
            return field.getValue().stream().map(valueField -> parseCurrency(dataMap, field.getKey(), valueField));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    };

    public BigDecimal calculate(Map<String,String> dataMap, Map<String, List<String>> addFields, Map<String, List<String>> subFields){
        BigDecimal toAdd = sumFields.apply(dataMap, addFields);
        BigDecimal toSubtract = sumFields.apply(dataMap, subFields);

        return toAdd.subtract(toSubtract);
    }

    private BigDecimal parseCurrency(Map<String,String> dataMap, String claimedKey, String valueKey){
        try {
            if (StringUtils.isBlank(claimedKey) || dataMap.getOrDefault(claimedKey, "").equalsIgnoreCase("YES")) {
                return new BigDecimal(dataMap.getOrDefault(valueKey, "0"));
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
