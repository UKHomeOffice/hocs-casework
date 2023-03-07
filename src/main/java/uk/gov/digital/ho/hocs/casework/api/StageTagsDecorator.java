package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StageTagsDecorator  {

    private static final String HOME_SEC_REPLY_FIELD_NAME = "HomeSecReply";

    public List<String> decorateTags(Map<String, String> data) {
        return Boolean.TRUE.equals(hasDataFieldAndValue(data, HOME_SEC_REPLY_FIELD_NAME, "TRUE")) ?
             List.of(StageTags.HOME_SEC_REPLY_TAG) :
             List.of();
    }

    private Boolean hasDataFieldAndValue(Map<String, String> data, String field, String value) {
        var dataValue = data.get(field);

        if (dataValue != null) {
            return dataValue.equals(value);
        }

        return false;
    }

}
