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

INSERT INTO `user` (`id`, `user_uuid`, `first_name`, `last_name`, `email`, `password`)
VALUES (1, '11111111-1111-4111-8111-111111111111', 'Hans', 'Muster', 'hans.muster@bbw.ch',
        '$2a$10$43ZvtIWKwneayY/SpH/R1.Qej0X5O1jOyQltuEmJMofcpOK2FWkCC'),
       (2, '22222222-2222-4222-8222-222222222222', 'Paula', 'Kuster', 'paula.kuster@bbw.ch',
        '$2a$10$iuxS2Z/CQ.ZOMSUUHc.0yeW0HzzwwuEV6RWOdsEk66difL09YZapm'),
       (3, '33333333-3333-4333-8333-333333333333', 'Andrea', 'Oester', 'andrea.oester@bbw.ch',
        '$2a$10$GoAxY1fjGTqvjlpkSKVWJOomugKO/UT45gIOgTWRyurF5P1SJlqa2');

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
