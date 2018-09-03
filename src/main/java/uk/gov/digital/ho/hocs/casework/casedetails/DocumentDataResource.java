package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.Set;
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

    @PostMapping(value = "/case/{caseUUID}/document", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateDocumentResponse> createDocument(@PathVariable UUID caseUUID,
            @RequestBody CreateDocumentRequest request) {
        DocumentData documentData = stageDataService.createDocument(caseUUID, request.getName(), request.getType());
        return ResponseEntity.ok(CreateDocumentResponse.from(documentData));
    }

    @PostMapping(value = "/case/{caseUUID}/document/{documentUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateDocument(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID,
            @RequestBody UpdateDocumentRequest request) {
        stageDataService.updateDocument(documentUUID, request.getStatus(), request.getFileLink(), request.getPdfLink());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/document", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetDocumentsResponse> getDocumentsForCase(@PathVariable UUID caseUUID) {
        Set<DocumentData> documents = stageDataService.getDocumentsForCase(caseUUID);
        return ResponseEntity.ok(GetDocumentsResponse.from(documents));
    }

    @GetMapping(value = "/case/{caseUUID}/document/{documentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getDocumentResourceLocation(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        return ResponseEntity.ok().build();
    }
}