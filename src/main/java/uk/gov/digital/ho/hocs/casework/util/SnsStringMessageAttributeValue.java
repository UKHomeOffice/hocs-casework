package uk.gov.digital.ho.hocs.casework.util;

import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import org.springframework.util.StringUtils;

public class SnsStringMessageAttributeValue {
    //extends MessageAttributeValue {

    private static final String type = "String";

    public SnsStringMessageAttributeValue(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Value should be a non-empty String");
        }


//        this.dataType(type);
//        this.stringValue(value);

    }

}
