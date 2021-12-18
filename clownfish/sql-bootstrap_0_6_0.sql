USE clownfish;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `maintenance` TINYINT NOT NULL DEFAULT '1' AFTER `searchrelevant`;

COMMIT;