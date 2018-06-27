ALTER TABLE audit add column document_id Int;

ALTER TABLE audit add constraint fk_document_data_id FOREIGN KEY (document_id) references audit_document_data(id)