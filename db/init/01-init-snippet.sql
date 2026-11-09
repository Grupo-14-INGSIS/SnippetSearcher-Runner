-- Reglas de formateo y linteo de cada usuario, por lenguaje (set_language: "language" es palabra reservada).
-- Cada regla es una fila de *_rule_values, con su tipo ('boolean', 'number' o 'string').
-- Coincide con las entidades FormattingRule, LintingRule y RuleValue.
CREATE TABLE IF NOT EXISTS formatting_rules
(
    user_id      VARCHAR(255) NOT NULL,
    set_language VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, set_language)
);

CREATE TABLE IF NOT EXISTS formatting_rule_values
(
    user_id      VARCHAR(255) NOT NULL,
    set_language VARCHAR(255) NOT NULL,
    rule_name    VARCHAR(255) NOT NULL,
    rule_value   TEXT         NOT NULL,
    value_type   VARCHAR(16)  NOT NULL,
    PRIMARY KEY (user_id, set_language, rule_name),
    FOREIGN KEY (user_id, set_language) REFERENCES formatting_rules (user_id, set_language) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS linting_rules
(
    user_id      VARCHAR(255) NOT NULL,
    set_language VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, set_language)
);

CREATE TABLE IF NOT EXISTS linting_rule_values
(
    user_id      VARCHAR(255) NOT NULL,
    set_language VARCHAR(255) NOT NULL,
    rule_name    VARCHAR(255) NOT NULL,
    rule_value   TEXT         NOT NULL,
    value_type   VARCHAR(16)  NOT NULL,
    PRIMARY KEY (user_id, set_language, rule_name),
    FOREIGN KEY (user_id, set_language) REFERENCES linting_rules (user_id, set_language) ON DELETE CASCADE
);
