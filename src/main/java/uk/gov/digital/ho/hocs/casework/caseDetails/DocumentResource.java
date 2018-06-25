package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class DocumentResource {

    private final DocumentService documentService;

    @Autowired
    public DocumentResource(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RequestMapping(value = "/case/{caseUUID}/document", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DocumentResponse> AddDocument(@PathVariable UUID caseUUID, @RequestBody DocumentRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            DocumentData documentData = documentService.addDocument(caseUUID, request.getDocumentUUID(), request.getDocumentDisplayName(), request.getDocumentType(), username);
            return ResponseEntity.ok(DocumentResponse.from(documentData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
