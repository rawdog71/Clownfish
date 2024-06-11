USE clownfish;

ALTER TABLE `clownfish`.`cf_site`
ADD COLUMN `isloginsite` TINYINT(1) NULL DEFAULT 0 AFTER `invisible`;
