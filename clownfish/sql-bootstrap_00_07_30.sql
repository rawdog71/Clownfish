USE clownfish;

ALTER TABLE `clownfish`.`cf_classcontent` 
ADD INDEX `classref` (`classref` ASC) VISIBLE;

ALTER TABLE `clownfish`.`cf_attribut` 
ADD COLUMN `nodelete` TINYINT(1) NULL DEFAULT '0' AFTER `description`;

