USE clownfish;
ALTER TABLE `clownfish`.`cf_attributetype` 
ADD COLUMN `canidentity` TINYINT NOT NULL DEFAULT 0 AFTER `searchrelevant`;

UPDATE `clownfish`.`cf_attributetype` SET `canidentity` = '1' WHERE (`id` = '2');
UPDATE `clownfish`.`cf_attributetype` SET `canidentity` = '1' WHERE (`id` = '3');
