CREATE UNIQUE INDEX principal_environment_name_application_name_business_group_name
    ON principal (environment_name, application_name, cluster, business_group, name);

CREATE UNIQUE INDEX principal_name_userId
    ON principal (name, user_id);
