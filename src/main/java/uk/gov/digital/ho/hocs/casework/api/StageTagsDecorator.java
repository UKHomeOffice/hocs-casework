package uk.gov.digital.ho.hocs.casework.api;

import java.util.List;
import java.util.Map;

public interface StageTagsDecorator {

    List<String> decorateTags(Map<String, String> data, String stageType);

}
