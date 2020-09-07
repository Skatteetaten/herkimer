CREATE TABLE RESOURCE
(
    id               SERIAL PRIMARY KEY,
    kind             VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    owner_id          VARCHAR(10) NOT NULL,
    created_date     TIMESTAMP,
    created_by       VARCHAR(255),
    modified_date    TIMESTAMP,
    modified_by      VARCHAR(255)
)

