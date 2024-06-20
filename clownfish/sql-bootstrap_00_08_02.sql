USE clownfish;

ALTER TABLE `clownfish`.`cf_javascript` 
ADD COLUMN `type` INT NULL DEFAULT '0' AFTER `invisible`;

UPDATE `clownfish`.`cf_javascript` SET `type` = 0;