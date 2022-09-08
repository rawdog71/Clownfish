USE clownfish;

ALTER TABLE `clownfish`.`cf_attribut` 
ADD COLUMN `relationtype` TINYINT(1) NULL DEFAULT NULL AFTER `isindex`;

COMMIT;
