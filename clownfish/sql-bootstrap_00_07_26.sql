USE clownfish;

CREATE TABLE `clownfish`.`cf_staticsite` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `site` VARCHAR(64) NULL,
  `urlparams` VARCHAR(1024) NULL,
  `generated` DATETIME NULL,
  PRIMARY KEY (`id`));