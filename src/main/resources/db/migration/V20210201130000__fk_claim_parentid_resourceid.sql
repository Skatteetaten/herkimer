ALTER TABLE resource
    ADD CONSTRAINT fk_parentid_resourceid FOREIGN KEY (parent_id) REFERENCES resource(id);