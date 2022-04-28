USE clownfish;

ALTER TABLE `cf_javaversion` 
CHANGE COLUMN `content` `content` LONGBLOB NULL DEFAULT NULL ;

ALTER TABLE `cf_classcontent` 
ADD COLUMN `checkedoutby` BIGINT NULL DEFAULT '0' AFTER `scrapped`;

CREATE TABLE IF NOT EXISTS `cf_contentversion` (
  `contentref` BIGINT UNSIGNED NOT NULL,
  `version` BIGINT NOT NULL,
  `content` LONGBLOB NULL,
  `tstamp` DATETIME NULL,
  `commitedby` BIGINT NULL,
  PRIMARY KEY (`contentref`, `version`));

COMMIT;