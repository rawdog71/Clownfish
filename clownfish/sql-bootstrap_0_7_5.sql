USE clownfish;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `templateref` BIGINT(20) NULL DEFAULT NULL AFTER `maintenance`;

COMMIT;