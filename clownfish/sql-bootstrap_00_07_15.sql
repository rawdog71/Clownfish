USE clownfish;

ALTER TABLE `clownfish`.`cf_attribut` 
ADD COLUMN `relationtype` TINYINT(1) DEFAULT 0 AFTER `isindex`;

COMMIT;
