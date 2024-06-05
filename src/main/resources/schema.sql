CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT NOT NULL PRIMARY KEY,
    username      TEXT   NOT NULL NOT NULL UNIQUE,
    currency_code TEXT   NOT NULL DEFAULT 'RUB'
);

CREATE TABLE IF NOT EXISTS category
(
    id      SERIAL,
    user_id BIGINT  NOT NULL REFERENCES users (id),
    title   TEXT    NOT NULL,
    hidden  BOOLEAN NOT NULL DEFAULT false,

    PRIMARY KEY (user_id, id)
);

CREATE TABLE IF NOT EXISTS transaction
(
    id          BIGSERIAL,
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    amount      DECIMAL   NOT NULL,
    category    INT       NOT NULL,
    description TEXT,
    type        TEXT      NOT NULL,
    startpoint  TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, id)
);

CREATE INDEX IF NOT EXISTS transaction_startpoint_user_id ON transaction USING brin (startpoint, user_id);