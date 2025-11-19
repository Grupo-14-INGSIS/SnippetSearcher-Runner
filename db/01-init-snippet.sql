CREATE TABLE IF NOT EXISTS rules(
    id_user VARCHAR,
    set_language VARCHAR,
    config_rules JSONB
);


--set language en vez de language porque es palabra reservada