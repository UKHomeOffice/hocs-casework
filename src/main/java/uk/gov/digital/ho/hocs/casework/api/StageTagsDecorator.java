package uk.gov.digital.ho.hocs.casework.api;

import java.util.Collection;
import java.util.Map;

public interface StageTagsDecorator {
    Collection<String> decorateTags(Map<String,String> data, String stageType);
}
