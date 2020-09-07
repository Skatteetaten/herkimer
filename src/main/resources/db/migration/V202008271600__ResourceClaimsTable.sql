CREATE TABLE RESOURCE_CLAIM
(
    id            SERIAL PRIMARY KEY,
    owner_id      VARCHAR(10)   NOT NULL,
    resource_id   bigint NOT NULL,
    credentials   VARCHAR   NOT NULL,
    created_date  TIMESTAMP,
    created_by    VARCHAR(255),
    modified_date TIMESTAMP,
    modified_by   VARCHAR(255)
)

