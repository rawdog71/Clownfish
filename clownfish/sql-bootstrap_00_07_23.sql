USE clownfish;

ALTER TABLE `clownfish`.`cf_attribut` 
ADD COLUMN `min_val` INT NULL DEFAULT '0' AFTER `relationtype`,
ADD COLUMN `max_val` INT NULL DEFAULT '0' AFTER `min_val`,
ADD COLUMN `default_val` VARCHAR(255) NULL AFTER `max_val`,
ADD COLUMN `mandatory` TINYINT(1) NULL DEFAULT '0' AFTER `default_val`,
ADD COLUMN `description` VARCHAR(255) NULL AFTER `mandatory`;