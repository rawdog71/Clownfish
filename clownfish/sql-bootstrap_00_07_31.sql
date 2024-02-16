USE clownfish;

ALTER TABLE `clownfish`.`cf_datasource` 
ADD COLUMN `restservice` TINYINT(4) NOT NULL DEFAULT '0' AFTER `driverclass`;