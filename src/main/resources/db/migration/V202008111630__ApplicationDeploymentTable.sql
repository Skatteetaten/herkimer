CREATE TABLE PRINCIPAL
(
    id               VARCHAR(10) PRIMARY KEY,
    type             VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    environment_name VARCHAR(255),
    application_name VARCHAR(255),
    business_group   VARCHAR(255),
    cluster          VARCHAR(40),
    user_id          VARCHAR(40),
    created_date     TIMESTAMP,
    created_by       VARCHAR(255),
    modified_date    TIMESTAMP,
    modified_by      VARCHAR(255)
)
