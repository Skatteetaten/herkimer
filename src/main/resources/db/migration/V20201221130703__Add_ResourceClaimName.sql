ALTER TABLE resource_claim
    ADD COLUMN name VARCHAR(255);

UPDATE resource_claim
    SET name = 'DEFAULT';

ALTER TABLE resource_claim
    ALTER COLUMN name SET NOT NULL;

DROP INDEX resourceclaim_ownerid_resourceid_credentials;

CREATE UNIQUE INDEX resourceclaim_ownerid_resourceid_credentials_name
    ON resource_claim (owner_id, resource_id, credentials, name);