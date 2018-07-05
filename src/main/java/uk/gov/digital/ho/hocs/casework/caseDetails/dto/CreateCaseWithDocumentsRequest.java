package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseWithDocumentsRequest {

    private CaseType caseType;
    private List<DocumentSummary> documents;

}
