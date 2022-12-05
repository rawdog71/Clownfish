USE clownfish;

ALTER TABLE `clownfish`.`cf_asset` 
ADD COLUMN `downloads` BIGINT DEFAULT 0 AFTER `filesize`;