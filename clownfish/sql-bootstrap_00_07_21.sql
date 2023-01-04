USE clownfish;

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `testparams` VARCHAR(255) NULL DEFAULT '' AFTER `shorturl`;