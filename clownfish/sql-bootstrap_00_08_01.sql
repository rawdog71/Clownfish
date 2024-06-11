USE clownfish;

ALTER TABLE `clownfish`.`cf_attribut` 
ADD COLUMN `ext_mutable` TINYINT(1) NULL DEFAULT '1' AFTER `nodelete`;

UPDATE `clownfish`.`cf_attribut` SET `ext_mutable` = 1;