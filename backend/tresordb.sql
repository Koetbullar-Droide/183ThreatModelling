DROP
DATABASE IF EXISTS tresordb;
CREATE
DATABASE tresordb;
USE
tresordb;

CREATE
USER IF NOT EXISTS 'tresoruser'@'%' IDENTIFIED BY 'tresorpassword';
GRANT ALL PRIVILEGES ON tresordb.* TO
'tresoruser'@'%';
FLUSH
PRIVILEGES;

CREATE TABLE user
(
    id         INT          NOT NULL AUTO_INCREMENT,
    user_uuid  VARCHAR(36)  NOT NULL,
    first_name VARCHAR(30)  NOT NULL,
    last_name  VARCHAR(30)  NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   LONGTEXT     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (user_uuid)
);

CREATE TABLE secret
(
    id          INT         NOT NULL AUTO_INCREMENT,
    secret_uuid VARCHAR(36) NOT NULL,
    user_id     INT         NOT NULL,
    content     JSON        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (secret_uuid),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

INSERT INTO `user` (`id`, `first_name`, 'user_uuid',`last_name`, `email`, `password`) VALUES
(1, 'Hans', '11111111-1111-4111-8111-111111111111', 'Muster', 'hans.muster@bbw.ch', '$2b$10$OP0LXMrAh8bgNrWilXayUOiGBIbI3yOZS4GavpKERfvrMmeF6ysOu'),
(2, 'Paula', '22222222-2222-4222-8222-222222222222', 'Kuster', 'paula.kuster@bbw.ch', '$2b$10$bq0gr9CO/bC3SybzTzkpjO8PiyH.ow6lx1oMFcdODUf/l1D2t3etC'),
(3, 'Andrea', '33333333-3333-4333-8333-333333333333', 'Oester', 'andrea.oester@bbw.ch', '$2b$10$tcBhFJQ4EKJd6P/L.KnL4ee63ygpyAa1ubHQZCbItBpaweUz18k5m');

INSERT INTO `secret` (`id`, `secret_uuid`, `user_id`, `content`)
VALUES (1, 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 1, '{
  "kindid": 1,
  "kind": "credential",
  "userName": "muster",
  "password": "1234",
  "url": "www.bbw.ch"
}'),
       (2, 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 1, '{
         "kindid": 2,
         "kind": "creditcard",
         "cardtype": "Visa",
         "cardnumber": "4242 4242 4242 4241",
         "expiration": "12/27",
         "cvv": "789"
       }'),
       (3, 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', 1, '{
         "kindid": 3,
         "kind": "note",
         "title": "Eragon",
         "content": "Und Eragon ging auf den Drachen zu."
       }');
