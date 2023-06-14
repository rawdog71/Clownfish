USE clownfish;

CREATE TABLE `clownfish`.`cf_api` (
  `siteref` BIGINT(20) NOT NULL,
  `keyname` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`siteref`, `keyname`));