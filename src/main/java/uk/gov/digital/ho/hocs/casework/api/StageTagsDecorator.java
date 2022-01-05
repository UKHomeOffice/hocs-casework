package uk.gov.digital.ho.hocs.casework.api;

import java.util.ArrayList;
import java.util.Map;

public interface StageTagsDecorator {
    ArrayList<String> decorateTags(Map<String,String> data, String stageType);
}
