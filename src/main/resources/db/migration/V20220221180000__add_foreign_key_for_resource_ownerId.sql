ALTER TABLE resource
    ADD CONSTRAINT fk_ownerId_principal FOREIGN KEY (owner_id) REFERENCES principal (id);