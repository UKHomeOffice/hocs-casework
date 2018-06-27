DROP TABLE IF EXISTS documentData cascade;

DROP INDEX IF EXISTS idx_document_uuid;
DROP INDEX IF EXISTS idx_document_casd_uuid;

DROP INDEX IF EXISTS idx_audit_document_document_type;
DROP INDEX IF EXISTS idx_audit_document_status;

DROP TABLE IF EXISTS audit_document cascade;

DROP INDEX IF EXISTS idx_audit_document_document_type;
DROP INDEX IF EXISTS idx_audit_document_status;
DROP INDEX IF EXISTS idx_audit_document_document_uuid;
DROP INDEX IF EXISTS idx_audit_document_case_uuid;
DROP INDEX IF EXISTS idx_audit_document_timestamp;