USE clownfish;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `encrypted` TINYINT NOT NULL DEFAULT '0' AFTER `templateref`;

COMMIT;