package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class DocumentDataResource {

    private final DocumentDataService stageDataService;

    @Autowired
    public DocumentDataResource(DocumentDataService stageDataService) {
        this.stageDataService = stageDataService;
    }

    @RequestMapping(value = "/case/{caseUUID}/document", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateDocumentResponse> createDocument(@PathVariable UUID caseUUID, @RequestBody CreateDocumentRequest request) {
        DocumentData documentData = stageDataService.createDocument(caseUUID, request.getName(), request.getType());
        return ResponseEntity.ok(CreateDocumentResponse.from(documentData));
    }

    @RequestMapping(value = "/case/{caseUUID}/document/{documentUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateDocument(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID, @RequestBody UpdateDocumentRequest request) {
        try {
            stageDataService.updateDocument(caseUUID, documentUUID, request.getStatus(), request.getFileLink(), request.getPdfLink());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
