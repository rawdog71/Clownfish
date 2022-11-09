USE clownfish;

ALTER TABLE `clownfish`.`cf_asset` 
ADD COLUMN `filesize` BIGINT DEFAULT 0 AFTER `uploadtime`;