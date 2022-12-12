USE clownfish;

ALTER TABLE `clownfish`.`cf_quartz`
ADD COLUMN `parameter` VARCHAR(2048) DEFAULT '' AFTER `active`;