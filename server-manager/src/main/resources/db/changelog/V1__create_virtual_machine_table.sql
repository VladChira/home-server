CREATE TABLE virtual_machine (
  id          BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(255)   NOT NULL UNIQUE,
  novnc_port  INT            NOT NULL,
  vm_port     INT            NOT NULL,
  base_path   VARCHAR(255)   NOT NULL
);