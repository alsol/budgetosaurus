CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT NOT NULL PRIMARY KEY,
    username TEXT   NOT NULL NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS category
(
    id      SERIAL,
    user_id BIGINT  NOT NULL REFERENCES users (id),
    title   TEXT    NOT NULL,
    hidden  BOOLEAN NOT NULL DEFAULT false,

    PRIMARY KEY (user_id, id)
);

CREATE TABLE IF NOT EXISTS expense
(
    id          BIGSERIAL,
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    amount      DECIMAL   NOT NULL,
    category    INT       NOT NULL,
    description TEXT,
    startpoint  TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, id)
);