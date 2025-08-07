CREATE TABLE service (
  `key`         VARCHAR(255)   NOT NULL,
  service_name  VARCHAR(255)   NOT NULL,
  owner         VARCHAR(255)   NOT NULL,
  description   TEXT           NOT NULL,
  created_at    VARCHAR(255)   NOT NULL,
  ip            VARCHAR(45),
  port          INT,
  domain        VARCHAR(255),
  notes         TEXT,
  PRIMARY KEY (`key`)
);

CREATE TABLE service_tags (
  service_key   VARCHAR(255)   NOT NULL,
  tag           VARCHAR(100)   NOT NULL,
  PRIMARY KEY (service_key, tag),
  FOREIGN KEY (service_key) REFERENCES service(`key`) ON DELETE CASCADE
);
