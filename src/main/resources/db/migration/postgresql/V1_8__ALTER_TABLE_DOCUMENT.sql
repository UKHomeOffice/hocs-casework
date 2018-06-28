ALTER TABLE document_data ALTER COLUMN s3_orig_link DROP NOT NULL;
ALTER TABLE document_data ALTER COLUMN s3_pdf_link DROP NOT NULL;

ALTER TABLE audit_document_data ALTER COLUMN s3_orig_link DROP NOT NULL;
ALTER TABLE audit_document_data ALTER COLUMN s3_pdf_link DROP NOT NULL;