DROP INDEX resource_kind_name_ownerid;

CREATE UNIQUE INDEX resource_kind_name_ownerid_active_cooldownAt
 on resource(owner_id, kind, name, active, set_to_cooldown_at) WHERE set_to_cooldown_at is NOT NULL;

CREATE UNIQUE INDEX resource_kind_name_ownerid_active
    on resource(owner_id, kind, name, active) WHERE set_to_cooldown_at is NULL;
