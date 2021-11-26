package uk.gov.digital.ho.hocs.casework.api;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

public interface StageTagsDecorator {

    void decorateTags(StageWithCaseData stage);
}
