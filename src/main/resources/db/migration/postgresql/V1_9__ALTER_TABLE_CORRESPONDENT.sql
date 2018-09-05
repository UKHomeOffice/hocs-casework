ALTER TABLE correspondent_data
  ADD COLUMN address_ident TEXT;

ALTER TABLE correspondent_data
  ADD CONSTRAINT correspondent_addressIdentity_idempotent UNIQUE (address_ident);

ALTER TABLE correspondent_data
  ADD COLUMN email_ident TEXT;

ALTER TABLE correspondent_data
  ADD CONSTRAINT correspondent_emailIdentity_idempotent UNIQUE (email_ident);

ALTER TABLE correspondent_data
  ADD COLUMN telephone_ident TEXT;

ALTER TABLE correspondent_data
  ADD CONSTRAINT correspondent_telephoneIdentity_idempotent UNIQUE (telephone_ident);