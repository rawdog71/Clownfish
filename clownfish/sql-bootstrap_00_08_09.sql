USE clownfish;
ALTER TABLE `clownfish`.`cf_attributetype` 
ADD COLUMN `canidentity` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `searchrelevant`;
ALTER TABLE `clownfish`.`cf_attributetype` 
ADD COLUMN `canautoinc` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `canidentity`;
ALTER TABLE `clownfish`.`cf_attributetype` 
ADD COLUMN `canindex` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `canautoinc`;

UPDATE `clownfish`.`cf_attributetype` SET `canidentity` = '1' WHERE (`id` = '2');
UPDATE `clownfish`.`cf_attributetype` SET `canidentity` = '1' WHERE (`id` = '3');

UPDATE `clownfish`.`cf_attributetype` SET `canautoinc` = '1' WHERE (`id` = '3');

UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '2');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '3');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '4');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '5');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '6');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '9');
UPDATE `clownfish`.`cf_attributetype` SET `canindex` = '1' WHERE (`id` = '10');
