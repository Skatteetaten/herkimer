ALTER TABLE resource
     ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE resource
    ADD COLUMN SET_TO_COOLDOWN_AT TIMESTAMP;