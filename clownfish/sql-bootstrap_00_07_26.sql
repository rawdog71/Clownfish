USE clownfish;

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `offline` TINYINT(1) NULL DEFAULT '0' AFTER `loginsite`;

CREATE TABLE `clownfish`.`cf_staticsite` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `site` VARCHAR(64) NULL,
  `urlparams` VARCHAR(1024) NULL,
  `tstamp` DATETIME NOT NULL,
  PRIMARY KEY (`id`));