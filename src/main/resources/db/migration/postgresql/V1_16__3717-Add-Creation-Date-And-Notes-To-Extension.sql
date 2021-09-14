ALTER TABLE case_deadline_extension
    ADD COLUMN created TIMESTAMP DEFAULT now(),
    ADD COLUMN note TEXT DEFAULT '';