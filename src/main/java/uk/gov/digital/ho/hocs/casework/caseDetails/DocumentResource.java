package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentsRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class DocumentResource {

    private final DocumentService documentService;

    @Autowired
    public DocumentResource(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RequestMapping(value = "/case/{caseUUID}/documents", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addDocuments(@PathVariable UUID caseUUID, @RequestBody AddDocumentsRequest request) {
        try {
            List<DocumentSummary> documentSummaries = request.getDocuments();
            documentService.addDocuments(caseUUID, documentSummaries);
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/document", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addDocument(@PathVariable UUID caseUUID, @RequestBody AddDocumentRequest request) {
        try {
            DocumentSummary documentSummary = request.getDocumentSummary();
            documentService.addDocument(caseUUID, documentSummary);
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
