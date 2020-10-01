ALTER TABLE PRINCIPAL
DROP COLUMN application_name;

CREATE UNIQUE INDEX principal_environment_name_business_group_name
    ON principal (environment_name, cluster, business_group, name);

CREATE UNIQUE INDEX resource_kind_name_ownerid
    ON resource (owner_id, name, kind);

CREATE UNIQUE INDEX resourceclaim_ownerid_resourceid_credentials
    ON resource_claim (owner_id, resource_id, credentials);