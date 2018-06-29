package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AddDocumentsRequest {

    public List<DocumentSummary> documents;

}
