USE clownfish;

ALTER TABLE `clownfish`.`cf_template` 
CHANGE COLUMN `layout` `type` TINYINT UNSIGNED NOT NULL DEFAULT '0' ;