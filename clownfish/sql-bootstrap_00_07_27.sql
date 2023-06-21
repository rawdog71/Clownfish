USE clownfish;

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `offline` TINYINT(1) NULL DEFAULT '0' AFTER `loginsite`;

ALTER TABLE `clownfish`.`cf_staticsite` 
ADD COLUMN `offline` TINYINT(1) NULL DEFAULT '0' AFTER `tstamp`;